"""
Common test infrastructure, domain simulation models, and verification utilities
for ExpenseShare v1.0 E2E Test Suite.
"""

from __future__ import annotations
import os
import re
import sys
import json
import time
from pathlib import Path
from typing import List, Dict, Optional, Tuple, Any, Callable
from dataclasses import dataclass, field
from enum import Enum

# Project Root Paths
REPO_ROOT = Path(__file__).resolve().parent.parent.parent
SERVER_DIR = REPO_ROOT / "server"
COMPOSE_APP_DIR = REPO_ROOT / "composeApp"
CORE_COMMON_DIR = REPO_ROOT / "core" / "common"
CORE_NETWORK_DIR = REPO_ROOT / "core" / "network"
CORE_DOMAIN_DIR = REPO_ROOT / "core" / "domain"
CORE_DATA_DIR = REPO_ROOT / "core" / "data"
CORE_CURRENCY_DIR = REPO_ROOT / "core" / "currency"
CORE_LOGGER_DIR = REPO_ROOT / "core" / "logger"
CORE_NAVIGATION_DIR = REPO_ROOT / "core" / "navigation"


# -----------------------------------------------------------------------------
# Domain Simulation Models (Reflecting Kotlin Multiplatform Core Models)
# -----------------------------------------------------------------------------

@dataclass(frozen=True)
class Amount:
    value: int  # Stored in smallest currency unit (e.g. cents / rials)

    def __add__(self, other: Amount | int) -> Amount:
        val = other.value if isinstance(other, Amount) else other
        return Amount(self.value + val)

    def __sub__(self, other: Amount | int) -> Amount:
        val = other.value if isinstance(other, Amount) else other
        return Amount(self.value - val)

    def __neg__(self) -> Amount:
        return Amount(-self.value)

    def is_zero(self) -> bool:
        return self.value == 0

    def is_positive(self) -> bool:
        return self.value > 0

    def is_negative(self) -> bool:
        return self.value < 0

    def __lt__(self, other: Amount) -> bool:
        return self.value < other.value

    def __le__(self, other: Amount) -> bool:
        return self.value <= other.value

    def __gt__(self, other: Amount) -> bool:
        return self.value > other.value

    def __ge__(self, other: Amount) -> bool:
        return self.value >= other.value

    def __repr__(self) -> str:
        return f"Amount({self.value})"


@dataclass(frozen=True)
class User:
    id: int
    username: str
    phone: str = ""

    def __hash__(self) -> int:
        return hash(self.id)

    def __eq__(self, other: object) -> bool:
        if not isinstance(other, User):
            return False
        return self.id == other.id


class TransactionStatus(Enum):
    PENDING = "PENDING"
    APPROVED = "APPROVED"
    REJECTED = "REJECTED"


class SettlementStatus(Enum):
    PENDING = "PENDING"
    PAID = "PAID"
    CONFIRMED = "CONFIRMED"
    DISPUTED = "DISPUTED"


@dataclass
class PayerRequest:
    user: User
    amount_paid: Amount


@dataclass
class ShareMemberRequest:
    user: User
    share: Amount


class ShareType(Enum):
    EQUAL = "Equal"
    EXACT = "Exact"
    PERCENTAGE = "Percentage"


@dataclass
class ShareDetailsRequest:
    type: ShareType
    members: List[ShareMemberRequest]


@dataclass
class Transaction:
    id: int
    group_id: int
    title: str
    amount: Amount
    created_by: int
    status: TransactionStatus = TransactionStatus.APPROVED
    payers: List[PayerRequest] = field(default_factory=list)
    share_details: Optional[ShareDetailsRequest] = None
    description: str = ""
    created_at: int = field(default_factory=lambda: int(time.time() * 1000))
    transaction_date: int = field(default_factory=lambda: int(time.time() * 1000))


@dataclass
class Settlement:
    id: int
    group_id: int
    debtor: User
    creditor: User
    amount: Amount
    status: SettlementStatus = SettlementStatus.PENDING
    created_at: int = field(default_factory=lambda: int(time.time() * 1000))
    updated_at: int = field(default_factory=lambda: int(time.time() * 1000))


# -----------------------------------------------------------------------------
# Settlement Calculation Engine (Opaque-box Mirror of Server SettlementService)
# -----------------------------------------------------------------------------

class SettlementEngine:
    @staticmethod
    def calculate_settlements(group_id: int, transactions: List[Transaction]) -> List[Settlement]:
        approved = [t for t in transactions if t.status == TransactionStatus.APPROVED]
        if not approved:
            return []

        paid: Dict[User, Amount] = {}
        share: Dict[User, Amount] = {}

        for trx in approved:
            for p in trx.payers:
                paid[p.user] = paid.get(p.user, Amount(0)) + p.amount_paid
            if trx.share_details and trx.share_details.members:
                for m in trx.share_details.members:
                    share[m.user] = share.get(m.user, Amount(0)) + m.share

        all_users = set(paid.keys()) | set(share.keys())
        balances: Dict[User, Amount] = {}
        for u in all_users:
            balances[u] = paid.get(u, Amount(0)) - share.get(u, Amount(0))

        total_net = sum(b.value for b in balances.values())
        if total_net != 0:
            raise ValueError(f"Net balance sum must be zero, got {total_net}")

        creditors: List[Tuple[User, int]] = sorted(
            [(u, b.value) for u, b in balances.items() if b.is_positive()],
            key=lambda x: x[1],
            reverse=True
        )
        debtors: List[Tuple[User, int]] = sorted(
            [(u, -b.value) for u, b in balances.items() if b.is_negative()],
            key=lambda x: x[1],
            reverse=True
        )

        settlements: List[Settlement] = []
        i = 0
        j = 0
        settle_id = 1

        while i < len(creditors) and j < len(debtors):
            creditor_user, cred_val = creditors[i]
            debtor_user, debt_val = debtors[j]

            settle_val = min(cred_val, debt_val)
            settlements.append(
                Settlement(
                    id=settle_id,
                    group_id=group_id,
                    debtor=debtor_user,
                    creditor=creditor_user,
                    amount=Amount(settle_val),
                    status=SettlementStatus.PENDING
                )
            )
            settle_id += 1

            new_cred_val = cred_val - settle_val
            new_debt_val = debt_val - settle_val

            creditors[i] = (creditor_user, new_cred_val)
            debtors[j] = (debtor_user, new_debt_val)

            if new_cred_val == 0:
                i += 1
            if new_debt_val == 0:
                j += 1

        return settlements

    @staticmethod
    def mark_as_paid(settlement: Settlement, user_id: int) -> bool:
        if settlement.debtor.id != user_id:
            return False
        if settlement.status != SettlementStatus.PENDING:
            return False
        settlement.status = SettlementStatus.PAID
        settlement.updated_at = int(time.time() * 1000)
        return True

    @staticmethod
    def confirm_payment(settlement: Settlement, user_id: int) -> bool:
        if settlement.creditor.id != user_id:
            return False
        if settlement.status != SettlementStatus.PAID:
            return False
        settlement.status = SettlementStatus.CONFIRMED
        settlement.updated_at = int(time.time() * 1000)
        return True

    @staticmethod
    def dispute_payment(settlement: Settlement, user_id: int) -> bool:
        if settlement.creditor.id != user_id:
            return False
        if settlement.status != SettlementStatus.PAID:
            return False
        settlement.status = SettlementStatus.PENDING
        settlement.updated_at = int(time.time() * 1000)
        return True


# -----------------------------------------------------------------------------
# Persistent Token & Session Storage Simulator
# -----------------------------------------------------------------------------

class PersistentTokenStorage:
    """
    Simulates Android SharedPreferences / JVM Preferences token persistence.
    """
    def __init__(self, backing_store: Optional[Dict[str, str]] = None):
        self._store: Dict[str, str] = backing_store if backing_store is not None else {}

    def get_token(self) -> Optional[Tuple[str, str]]:
        access = self._store.get("access_token")
        refresh = self._store.get("refresh_token")
        if access and refresh:
            return (access, refresh)
        return None

    def set_token(self, access_token: str, refresh_token: str) -> None:
        self._store["access_token"] = access_token
        self._store["refresh_token"] = refresh_token

    def clear_token(self) -> None:
        self._store.pop("access_token", None)
        self._store.pop("refresh_token", None)

    def restart_process(self) -> PersistentTokenStorage:
        """Simulate app process kill and restart with same persistent storage."""
        return PersistentTokenStorage(backing_store=dict(self._store))


# -----------------------------------------------------------------------------
# Verification & Assertion Utilities
# -----------------------------------------------------------------------------

class AssertionFailure(Exception):
    pass


def assert_true(condition: bool, message: str = "Expected True but got False") -> None:
    if not condition:
        raise AssertionFailure(message)


def assert_false(condition: bool, message: str = "Expected False but got True") -> None:
    if condition:
        raise AssertionFailure(message)


def assert_equal(actual: Any, expected: Any, message: str = "") -> None:
    if actual != expected:
        msg = f"Expected {expected!r}, but got {actual!r}."
        if message:
            msg += f" Details: {message}"
        raise AssertionFailure(msg)


def assert_raises(exc_type: type, func: Callable, *args: Any, **kwargs: Any) -> Any:
    try:
        func(*args, **kwargs)
    except exc_type as e:
        return e
    except Exception as e:
        raise AssertionFailure(f"Expected exception {exc_type.__name__}, but caught {type(e).__name__}: {e}")
    raise AssertionFailure(f"Expected exception {exc_type.__name__}, but no exception was raised")


def read_file(path: Path) -> str:
    if not path.is_file():
        raise FileNotFoundError(f"File not found: {path}")
    return path.read_text(encoding="utf-8")


def check_path_exists(path: Path) -> bool:
    return path.exists()


def search_in_file(path: Path, pattern: str, regex: bool = False) -> bool:
    if not path.is_file():
        return False
    content = read_file(path)
    if regex:
        return bool(re.search(pattern, content))
    return pattern in content
