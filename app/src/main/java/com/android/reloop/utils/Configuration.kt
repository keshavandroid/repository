package com.android.reloop.utils

import com.reloop.reloop.R
import com.reloop.reloop.app.MainApplication
import java.util.*

object Configuration {
    const val DB_VERSION: Long = 2
    private val environment = Environments.Production
    //private val environment = Environments.StagingNEW

    @JvmStatic
    val baseUrl: ArrayList<String>
        get() {
            val links = ArrayList<String>()
            return when (environment) {
               /* Environments.Development -> {
                    links.add(
                        MainApplication.applicationContext().getString(R.string.development_reloop)
                    )
                    links.add(
                       MainApplication.applicationContext()
                            .getString(R.string.development_reloop_image)
                    )
                    links.add(
                        MainApplication.applicationContext()
                            .getString(R.string.development_reloop_deeplink)
                    )
                    links
                }*/
                Environments.Production -> {
                    links.add(
                        MainApplication.applicationContext().getString(R.string.production_reloop)
                    )
                    links.add(
                        MainApplication.applicationContext()
                            .getString(R.string.production_reloop_image)
                    )
                    links.add(
                        MainApplication.applicationContext()
                            .getString(R.string.production_reloop_deepLink)
                    )
                    links
                }
               /* Environments.Staging -> {
                    links.add(
                        MainApplication.applicationContext().getString(R.string.staging_reloop)
                    )
                    links.add(
                        MainApplication.applicationContext()
                            .getString(R.string.staging_reloop_image)
                    )
                    links.add(
                        MainApplication.applicationContext()
                            .getString(R.string.staging_reloop_deepLink)
                    )
                    links
                }*/
                Environments.StagingNEW -> {
                    links.add(
                        MainApplication.applicationContext().getString(R.string.staging_reloop_new)
                    )
                    links.add(
                        MainApplication.applicationContext()
                            .getString(R.string.staging_reloop_image_new)
                    )
                    links.add(
                        MainApplication.applicationContext()
                            .getString(R.string.staging_reloop_deepLink_new)
                    )
                    links
                }
                else -> {
                    links.add("Dummy Link")
                    links.add("Dummy Link")
                    links.add("Dummy Link")
                    links
                }
            }
        }

    val isProduction: Boolean
        get() {
            return try {
                environment == Environments.Production
            } catch (e: Exception) {
                false
            }
        }

    val isDevelopment: Boolean
        get() {
            return try {
                environment == Environments.Development
            } catch (e: Exception) {
                false
            }
        }

    val isStagingNew: Boolean
        get() {
            return try {
                environment == Environments.StagingNEW
            } catch (e: Exception) {
                false
            }
        }

    val environmentName: String
        get() = environment.toString()

    enum class Environments {
        Development, Production, Local, Staging, StagingNEW
    }
}