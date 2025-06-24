// WeatherResponse.kt
package com.example.si_1.network

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "response", strict = false)
data class WeatherResponse(
    @field:Element(name = "body", required = false)
    var body: Body? = null
)

@Root(name = "body", strict = false)
data class Body(
    @field:Element(name = "items", required = false)
    var items: Items? = null
)

@Root(name = "items", strict = false)
data class Items(
    @field:ElementList(inline = true, required = false)
    var item: List<WeatherItem>? = null
)

@Root(name = "item", strict = false)
data class WeatherItem(
    @field:Element(name = "category", required = false)
    var category: String? = null,

    @field:Element(name = "fcstValue", required = false)
    var fcstValue: String? = null
)
