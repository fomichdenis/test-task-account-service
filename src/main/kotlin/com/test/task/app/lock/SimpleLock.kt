package com.test.task.app.lock

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

private val usedKeys = ConcurrentHashMap<String, Lock>()

fun createSimpleLockForAccount(id: Long): SimpleLock {
   return SimpleLock("account:$id")
}

class SimpleLock(
   private val key: String
) {
   fun tryLock(seconds: Long, timeUnit: TimeUnit): Boolean {
      val newLock = ReentrantLock()
      val lock = usedKeys.putIfAbsent(key, newLock)
      return lock?.tryLock(seconds, timeUnit) ?: newLock.tryLock(seconds, timeUnit)
   }

   fun unlock() {
      val lock = usedKeys.remove(key)
      lock?.unlock()
   }
}