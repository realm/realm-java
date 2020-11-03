package io.realm.examples.coroutinesexample.data.newsreader.local.room

import io.realm.examples.coroutinesexample.data.newsreader.network.model.NYTimesArticle
import io.realm.examples.coroutinesexample.domain.newsreader.model.DomainNYTArticle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun RoomNYTDao.getArticlesFlow(apiSection: String): Flow<List<DomainNYTArticle>> {
    return getArticles(apiSection).toDomainFlow()
}

suspend fun RoomNYTDao.insertArticles(
        apiSection: String,
        articles: List<NYTimesArticle>
) {
    deleteArticles(apiSection)
    val roomArticles = articles.toRoomArticles(apiSection).take(10)
    insertArticles(roomArticles)
}

private fun Flow<List<RoomNYTimesArticle>>.toDomainFlow(): Flow<List<DomainNYTArticle>> {
    return map { realmList ->
        realmList.map { realmArticle ->
            DomainNYTArticle(realmArticle.title, realmArticle.abstract)
        }
    }
}

private fun List<NYTimesArticle>.toRoomArticles(apiQuerySection: String): List<RoomNYTimesArticle> {
    return map { article ->
        RoomNYTimesArticle().apply {
            apiSection = apiQuerySection
            section = article.section
            subsection = article.subsection
            title = article.title
            abstract = article.abstract
            url = article.url
        }
    }
}
