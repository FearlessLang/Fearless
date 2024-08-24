#!/bin/sh
# RT Lib
CROSS_CONTAINER_OPTS="--platform linux/amd64" cross build --features 'runtime-only' --release --target x86_64-unknown-linux-gnu
cp target/x86_64-unknown-linux-gnu/release/libnative_rt.so ../artefacts/rt/libnative/amd64-libnative_rt.so

#cross build  --features 'runtime-only' --release --target aarch64-unknown-linux-gnu
#cp target/aarch64-unknown-linux-gnu/release/libnative_rt.so ../artefacts/rt/libnative/arm64-libnative_rt.so
#
#CROSS_CONTAINER_OPTS="--platform linux/amd64" cross build  --features 'runtime-only' --release --target x86_64-pc-windows-gnu
#cp target/x86_64-pc-windows-gnu/release/native_rt.dll ../artefacts/rt/libnative/amd64-native_rt.dll
#
#cross build  --features 'runtime-only' --release --target aarch64-apple-darwin
#cp target/aarch64-apple-darwin/release/libnative_rt.dylib ../artefacts/rt/libnative/arm64-libnative_rt.dylib
#
#cross build  --features 'runtime-only' --release --target x86_64-apple-darwin
#cp target/x86_64-apple-darwin/release/libnative_rt.dylib ../artefacts/rt/libnative/amd64-libnative_rt.dylib
#
## Compiler Lib
#CROSS_CONTAINER_OPTS="--platform linux/amd64" cross build --features 'compiler-only' --release --target x86_64-unknown-linux-gnu
#cp target/x86_64-unknown-linux-gnu/release/libnative_rt.so ../artefacts/rt/libnative/amd64-libnative_compiler.so
#
#cross build  --features 'compiler-only' --release --target aarch64-unknown-linux-gnu
#cp target/aarch64-unknown-linux-gnu/release/libnative_rt.so ../artefacts/rt/libnative/arm64-libnative_compiler.so
#
#CROSS_CONTAINER_OPTS="--platform linux/amd64" cross build  --features 'compiler-only' --release --target x86_64-pc-windows-gnu
#cp target/x86_64-pc-windows-gnu/release/native_rt.dll ../artefacts/rt/libnative/amd64-native_compiler.dll
#
#cross build  --features 'compiler-only' --release --target aarch64-apple-darwin
#cp target/aarch64-apple-darwin/release/libnative_rt.dylib ../artefacts/rt/libnative/arm64-libnative_compiler.dylib
#
#cross build  --features 'compiler-only' --release --target x86_64-apple-darwin
#cp target/x86_64-apple-darwin/release/libnative_rt.dylib ../artefacts/rt/libnative/amd64-libnative_compiler.dylib
