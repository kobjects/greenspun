package org.kobjects.greenspun.demo


import org.kobjects.greenspun.runtime.ImportObject
import org.kobjects.greenspun.core.module.Module
import org.kobjects.greenspun.core.type.Bool
import org.kobjects.greenspun.core.type.I32
import org.kobjects.greenspun.wasi.addStdIoImpl

@OptIn(ExperimentalStdlibApi::class)
fun main(argv: Array<String>) {

    val fizzBuzzModule = Module {

        val memory = Memory(1)

        // Helpers for printing strings and numbers via fd_write
        val digit = memory.data("0")
        val write_result = memory.data("1234")

        val FdWrite = ImportFunc("wasi_snapshot_preview1", "fd_write", I32) { Param(I32, I32, I32, I32) }

        val Print = Func {
            val address = Param(I32)
            val len = Param(I32)
            Drop(FdWrite(1, address, len, write_result))
        }

        val PrintI32Inner = ForwardDecl() { Param(I32, Bool) }

        Implementation(PrintI32Inner) {
            val i = Param(I32)
            val uncoditional = Param(Bool)
            If ((i Gt 0) Or uncoditional) {
                PrintI32Inner(i / 10, false)
                memory.u8i32[digit] = i % 10 + 48
                Print(digit, 1)
            }
        }

        val PrintI32 = Func {
            val i = Param(I32)
            PrintI32Inner(i, true)
        }

        // "Actual" FizzBuzz

        val fizz = memory.data("Fizz")
        val buzz = memory.data("Buzz")
        val newline = memory.data("\n")

        val fizzBuzzFunc = Func {
            For(1, 21) { count ->
                If(count % 3 Eq 0) {
                    Print(fizz, fizz.len)
                    If(count % 5 Eq 0) {
                        Print(buzz, buzz.len)
                    }
                }.Else {
                    If(count % 5 Eq 0) {
                        Print(buzz, buzz.len)
                    }.Else {
                        PrintI32(count)
                    }
                }
                Print(newline, 1)
            }
        }

        Start(fizzBuzzFunc)
        // Export("fizzBuzz", fizzBuzzFunc)
    }

    println("FizzBuzz wasm binary code:")
    println(fizzBuzzModule.toWasm().toHexString(HexFormat {
        bytes.bytesPerLine = 32
        bytes.bytesPerGroup = 16
        bytes.groupSeparator = "  "
    }))

    println()
    println("Instantiating module:")
    println()

    val importObject = ImportObject()
    importObject.addStdIoImpl()

    fizzBuzzModule.instantiate(importObject)

    // val fizzBuzzInstance = fizzBuzzModule.instantiate(importObject)
    // fizzBuzzInstance.funcExports["fizzBuzz"]!!()
}
