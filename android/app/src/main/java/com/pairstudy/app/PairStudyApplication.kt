package com.pairstudy.app
import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.pairstudy.app.data.api.RetrofitClient
import com.pairstudy.app.data.local.SessionStore
import com.pairstudy.app.data.repository.StudyRepository
class PairStudyApplication : Application(), ImageLoaderFactory {
    val session by lazy { SessionStore(this) }
    val client by lazy { RetrofitClient { session.token } }
    val repository by lazy { StudyRepository(client.api, session) }
    // Private images are not persisted to Coil's disk cache.
    override fun newImageLoader() = ImageLoader.Builder(this).okHttpClient(client.http).diskCachePolicy(coil.request.CachePolicy.DISABLED).build()
}
