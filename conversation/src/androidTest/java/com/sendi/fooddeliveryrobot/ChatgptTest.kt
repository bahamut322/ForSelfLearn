//package com.sendi.fooddeliveryrobot
//
//import com.plexpt.chatgpt.ChatGPT
//import com.plexpt.chatgpt.util.Proxys
//import kotlinx.coroutines.runBlocking
//import org.junit.Test
//import java.net.Proxy
//
//class ChatgptTest {
////    @Test
////    fun test() = runBlocking{
////        //国内需要代理
////        //国内需要代理
////        val proxy: Proxy? = Proxys.http("192.168.62.20", 1080)
////        //socks5 代理
////        // Proxy proxy = Proxys.socks5("127.0.0.1", 1080);
////
////        //socks5 代理
////        // Proxy proxy = Proxys.socks5("127.0.0.1", 1080);
////        val chatGPT: ChatGPT = ChatGPT.builder()
////            .apiKey("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")
////            .proxy(proxy)
////            .apiHost("https://api.openai.com/") //反向代理地址
////            .build()
////            .init()
////
////        val res: String = chatGPT.chat("付气化是什么意思")
////        println(res)
////        while (true){
////
////        }
////    }
//}