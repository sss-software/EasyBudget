/*
 *   Copyright 2020 Benoit LETONDOR
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.benoitletondor.easybudgetapp.injection

import androidx.collection.ArrayMap
import com.benoitletondor.easybudgetapp.auth.Auth
import com.benoitletondor.easybudgetapp.auth.FirebaseAuth
import com.benoitletondor.easybudgetapp.cloudstorage.CloudStorage
import com.benoitletondor.easybudgetapp.cloudstorage.FirebaseStorage
import com.benoitletondor.easybudgetapp.parameters.Parameters
import com.benoitletondor.easybudgetapp.iab.Iab
import com.benoitletondor.easybudgetapp.iab.IabImpl
import com.benoitletondor.easybudgetapp.model.Expense
import com.benoitletondor.easybudgetapp.db.DB
import com.benoitletondor.easybudgetapp.db.impl.CachedDBImpl
import com.benoitletondor.easybudgetapp.db.impl.CacheDBStorage
import com.benoitletondor.easybudgetapp.db.impl.DBImpl
import com.benoitletondor.easybudgetapp.db.impl.RoomDB
import org.koin.dsl.module
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

val appModule = module {
    single { Parameters(get()) }

    single<Iab> { IabImpl(get(), get()) }

    single<CacheDBStorage> { object : CacheDBStorage {
        override val expenses: MutableMap<Date, List<Expense>> = ArrayMap()
        override val balances: MutableMap<Date, Double> = ArrayMap()
    } }

    single<Executor> { Executors.newSingleThreadExecutor() }

    single<Auth> { FirebaseAuth(com.google.firebase.auth.FirebaseAuth.getInstance()) }

    single<CloudStorage> { FirebaseStorage(com.google.firebase.storage.FirebaseStorage.getInstance().apply {
        maxOperationRetryTimeMillis = TimeUnit.SECONDS.toMillis(10)
        maxDownloadRetryTimeMillis = TimeUnit.SECONDS.toMillis(10)
        maxUploadRetryTimeMillis = TimeUnit.SECONDS.toMillis(10)
    }) }

    factory<DB> { CachedDBImpl(DBImpl(RoomDB.create(get())), get(), get()) }

    factory { CachedDBImpl(DBImpl(RoomDB.create(get())), get(), get()) }
}