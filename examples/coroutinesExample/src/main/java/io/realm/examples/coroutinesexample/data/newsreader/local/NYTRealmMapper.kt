package io.realm.examples.coroutinesexample.data.newsreader.local

import io.realm.RealmList
import io.realm.examples.coroutinesexample.data.newsreader.network.model.NYTMultimedium
import io.realm.examples.coroutinesexample.data.newsreader.network.model.NYTimesArticle

suspend fun RealmNYTDao.insertArticles(apiSection: String, articles: List<NYTimesArticle>) {
    val realmArticles = articles.toRealmArticles(apiSection)
    insertArticles(realmArticles)
}

private fun List<NYTimesArticle>.toRealmArticles(apiQuerySection: String): List<RealmNYTimesArticle> {
    return map { article ->
        RealmNYTimesArticle().apply {
            apiSection = apiQuerySection
            section = article.section
            subsection = article.subsection
            title = article.title
            abstract = article.abstract
            url = article.url
            uri = article.uri
            byline = article.byline
            itemType = article.itemType
            updatedDate = article.updatedDate
            createDate = article.createDate
            publishedDate = article.publishedDate
            materialTypeFacet = article.materialTypeFacet
            kicker = article.kicker
            desFacet = RealmList<String>().apply { addAll(article.desFacet ?: listOf()) }
            orgFacet = RealmList<String>().apply { addAll(article.orgFacet ?: listOf()) }
            perFacet = RealmList<String>().apply { addAll(article.perFacet ?: listOf()) }
            geoFacet = RealmList<String>().apply { addAll(article.geoFacet ?: listOf()) }
            orgFacet = RealmList<String>().apply { addAll(article.orgFacet ?: listOf()) }
            perFacet = RealmList<String>().apply { addAll(article.perFacet ?: listOf()) }
            geoFacet = RealmList<String>().apply { addAll(article.geoFacet ?: listOf()) }
            multimedia = article.multimedia.toRealmMultimediumRealmList()
            shortUrl = article.shortUrl
        }
    }
}

private fun List<NYTMultimedium>?.toRealmMultimediumRealmList(): RealmList<RealmNYTMultimedium> {
    return RealmList<RealmNYTMultimedium>().also { realmList ->
        realmList.addAll(
                this?.map { multimedium ->
                    multimedium.toRealmMultimedium()
                } ?: listOf()
        )
    }
}

private fun NYTMultimedium.toRealmMultimedium(): RealmNYTMultimedium {
    return RealmNYTMultimedium().also {
        it.url = url
        it.format = format
        it.height = height
        it.width = width
        it.type = type
        it.subtype = subtype
        it.caption = caption
        it.copyright = copyright
    }
}
