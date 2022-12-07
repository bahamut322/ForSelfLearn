package com.sendi.deliveredrobot.model

class CommonListDataModel {
    var key = ""
    var value = ""
    var type = -1
    var position = -1

    constructor() {
    }

    constructor(key: String,value: String) {
        this.key = key
        this.value = value
    }

    constructor(key: String,value: String, type: Int) {
        this.key = key
        this.value = value
        this.type = type
    }

}


