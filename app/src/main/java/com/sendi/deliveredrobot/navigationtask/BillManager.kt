package com.sendi.deliveredrobot.navigationtask

import java.util.*

/**
 * @author heky
 * @date 2022-08-23
 * @description 任务清单类管理类
 */
object BillManager {
    private val billList = LinkedList<ITaskBill>() // 任务清单类链表

    /**
     * @description 获取当前bill
     */
    fun currentBill(): ITaskBill? {
        return billList.peekFirst()
    }

    /**
     * @description 获取下一个bill
     */
    fun findNextBill(): ITaskBill? {
        removeBill()
        return currentBill()
    }
    fun clearBillList(){
        billList.clear()
    }

    /**
     * @description 删除
     */
    fun removeBill(bill: ITaskBill? = null): ITaskBill? {
        return when (bill == null) {
            true -> billList.remove()
            false -> billList.removeAt(billList.indexOf(bill))
        }
    }

    fun clearBill(bill: ITaskBill? = null){
        when (bill == null) {
            true -> currentBill()?.removeAll()
            false -> billList[billList.indexOf(bill)].removeAll()
        }
    }

    /**
     * @description 添加末尾
     */
    fun addLast(bill: ITaskBill) {
        billList.add(bill)
    }

    /**
     * @description 添加末尾
     */
    fun addAllLast(inputBillList: List<ITaskBill>){
        billList.addAll(inputBillList)
    }

    /**
     * @description 添加index
     */
    fun addAllAtIndex(inputBillList: List<ITaskBill>, index: Int = 0){
        billList.addAll(index, inputBillList)
    }

    /**
     * @description billList
     */
    fun billList(): LinkedList<ITaskBill>{
        return billList
    }

    /**
     * @description 下一个任务地点
     */
    fun nextEndTarget(): String{
        return when(billList.size > 1){
            true -> {
                when (billList[1]) {
                    is DoubleSameSendTaskBillTwo -> {
                        if(billList.size > 2){
                            billList[2].endTarget()
                        }else{
                            ""
                        }
                    }
                    else -> billList[1].endTarget()
                }
            }
            false -> ""
        }
    }

    /**
     * @description 获取剩余任务数
     */
    fun remainTaskCount(): Int {
        val size = billList.size
        return when (size > 0) {
            true -> {
                var count: Int = billList.size
                for (iTaskBill in billList) {
                    if(iTaskBill is DoubleSameSendTaskBillTwo){
                        count--
                        break
                    }
                }
                var result = count - 1
                if(result < 0){
                    result = 0
                }
                result
            }
            else -> size
        }
    }
}