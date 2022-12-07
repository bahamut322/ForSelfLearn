package com.sendi.deliveredrobot.model

/**
 * @describe:高德天气
 * https://restapi.amap.com/v3/weather/weatherInfo
 * {"status":"1","count":"1","info":"OK","infocode":"10000","lives":[{"province":"广东","city":"广州市","adcode":"440100","weather":"小雨","temperature":"27","winddirection":"东南","windpower":"≤3","humidity":"98","reporttime":"2021-06-22 14:00:27"}]}
 */
data class GWeatherModel(
    var status: String,
    var count: String,
    var info: String,
    var infocode: String,
    var lives: List<Lives>
) {
    data class Lives(
        var province: String,
        var city: String,
        var adcode: String,
        var weather: String,
        var temperature: String,
        var winddirection: String,
        var windpower: String,
        var humidity: String,
        var reporttime: String
    )
}