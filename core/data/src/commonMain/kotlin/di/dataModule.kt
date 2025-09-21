package di

import org.koin.dsl.module

val dataModule = module {

}

val dataAggregator = module {
    includes(networkModule)
    includes(dataModule)
}