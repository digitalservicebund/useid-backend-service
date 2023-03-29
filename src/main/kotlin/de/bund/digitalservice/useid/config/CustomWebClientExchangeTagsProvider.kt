package de.bund.digitalservice.useid.config

import de.bund.digitalservice.useid.tenant.PARAM_NAME_TENANT_ID
import de.bund.digitalservice.useid.tenant.REQUEST_ATTR_TENANT
import de.bund.digitalservice.useid.tenant.tenants.Tenant
import io.micrometer.core.instrument.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcTagsContributor
import org.springframework.stereotype.Component

@Component
class CustomWebClientExchangeTagsProvider : WebMvcTagsContributor {

    override fun getTags(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        handler: Any?,
        exception: Throwable?,
    ): MutableIterable<Tag> {
        val tags = mutableListOf<Tag>()
        request?.let { r ->
            val tenant: Tenant? = r.getAttribute(REQUEST_ATTR_TENANT) as Tenant?
            tenant?.let {
                val tag = Tag.of(PARAM_NAME_TENANT_ID, tenant.id)
                tags.add(tag)
            }
        }
        return tags
    }

    override fun getLongRequestTags(request: HttpServletRequest?, handler: Any?): MutableIterable<Tag> {
        return mutableListOf()
    }
}
