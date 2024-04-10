package org.kobjects.greenspun

import org.kobjects.greenspun.core.module.Module
import org.kobjects.greenspun.core.type.Bool
import org.kobjects.greenspun.core.type.I32
import kotlin.test.*

class MemoryTest {

    @Test
    fun testSimpleMemory() {
        Module { Memory(0) }
        Module { Memory(1) }
        Module { Memory(0, 0) }
        Module { Memory(0, 1) }
        Module { Memory(1, 256) }
        Module { Memory(0, 65536) }
    }

    @Test
    fun testMultipleMemories() {
        assertFailsWith(
            exceptionClass = IllegalStateException::class,
            message = "multiple memories"
        ) {
            Module {
                Memory(0)
                Memory(0)
            }
        }

        assertFailsWith(
            exceptionClass = IllegalStateException::class,
            message = "multiple memories"
        ) {
            Module {
                ImportMemory("spectest", "memory", 0)
                Memory(0)
            }
        }
    }

    @Test
    fun testMemorySize() {
        assertEquals(
            0,
            Module {
                val memory = Memory(0)
                memory.data("")
                Export("memsize", Func(I32) { Return(memory.size) })
            }.instantiate().invoke("memsize")
        )

        assertEquals(
            1,
            Module {
                val memory = Memory(1)
                memory.data("x")
                Export("memsize", Func(I32) { Return(memory.size) })
            }.instantiate().invoke("memsize")
        )
    }

    @Test
    fun testMemory() {
        val module = Module {
            val mem = Memory(1)
            mem.data(0, "ABC\u00a7D")
            mem.data(20, "WASM")

            val data = Func(Bool) {
                Return((mem.i32Load8U(0) Eq 65) And
                        (mem.i32Load8U(3) Eq 167))

            }

        }
    }
}