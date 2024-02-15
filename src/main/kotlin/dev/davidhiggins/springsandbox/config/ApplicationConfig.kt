package dev.davidhiggins.springsandbox.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableWebMvc
class ApplicationConfig: WebMvcConfigurer {

    @Bean
    @Primary
    fun objectMapper(): ObjectMapper {
        val customModule = SimpleModule() // Add deserializer if needed
        return ObjectMapper()
            .registerModules(
                JavaTimeModule(),
                ParameterNamesModule(),
                KotlinModule.Builder().configure(KotlinFeature.StrictNullChecks, true).build()
            )
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
            .registerModules(customModule)
    }

}




