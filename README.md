lcc is a standard lambda calculus compiler/interpreter focusing on accuracy and a clean output.

## Runtime Dependencies

lcc is compiled with kotlinc 2.3.10 and so requires a corresponding compatible java runtime to be installed.
It works by first embedding the lambda calculus in a kotlin source with an embedded interpreter,
then compiling to java bytecode, so it also requires kotlinc to be available during runtime, as well as
a corresponding compatible java development kit.

## Build Dependencies/Instructions

kotlinc 2.3.10 and a corresponding compatible java development kit must be on the path.

To build, run:
```sh
make
```
To test, run:
```sh
make test
```
To clean, run:
```sh
make clean
```
To automatically install binaries to your ~/.local/bin folder, run:
```sh
make install
```
