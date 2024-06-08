package dev.davidhiggins.springsandbox

import dev.davidhiggins.springsandbox.customer.Customer
import dev.davidhiggins.springsandbox.customer.CustomerRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.opensearch.client.RestHighLevelClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

class DatabaseTest: RepositoryTest() {

    @Autowired
    lateinit var repository: CustomerRepository

    @Autowired
    lateinit var operations: ElasticsearchOperations

    @Autowired
    lateinit var restHighLevelClient: RestHighLevelClient

    @Test
    fun `Save and load`() {
        val entity = Customer(id = 1, name = "test")
        repository.save(entity)

        assertThat(repository.findById(entity.id!!).get()).isEqualTo(entity)
        assertThat(repository.findByName(entity.name)).isEqualTo(entity)
        assertThat(repository.findAll().first()).isEqualTo(entity)
    }


}

@ExtendWith(SpringExtension::class)
@SpringBootTest
@DirtiesContext
@ContextConfiguration(initializers = [RepositoryTest.Initializer::class])
@TestPropertySource("/application-test.properties")
@Testcontainers(disabledWithoutDocker = true)
class RepositoryTest {

    companion object {
        private val database = PostgreSQLContainer(DockerImageName.parse("postgres:latest"))
          .withDatabaseName("integration-tests-db")
          .withUsername("sa")
          .withPassword("sa");
    }

    internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            database.start()
            TestPropertyValues.of(
                "spring.datasource.url=" + database.jdbcUrl,
                "spring.datasource.username=" + database.username,
                "spring.datasource.password=" + database.password
            ).applyTo(configurableApplicationContext.environment);

            TestPropertyValues.of(
                "opensearch.uris=http://${database.host}:${database.firstMappedPort}"
            ).applyTo(configurableApplicationContext.environment)
        }
    }

}
