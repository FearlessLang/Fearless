use std::io;
use std::io::Write;

use jni::JNIEnv;
use jni::objects::{JByteArray, JClass};
use jni::sys::{jint, jintArray, jsize};
use unicode_segmentation::UnicodeSegmentation;

pub use fearless_str::FearlessStr;

mod fearless_str;

#[no_mangle]
pub extern "system" fn Java_rt_NativeRuntime_validateStringOrThrow<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>, utf8_str: JByteArray<'local>) {
    if let Some(err) = FearlessStr::new(&mut env, &utf8_str).validate() {
        env.throw_new("rt/NativeRuntime$StringEncodingError", format!("{}", err)).unwrap();
    }
}

/// # Safety
/// You have invoked `NativeRuntime.validateStringOrThrow()` before calling this method.
#[no_mangle]
pub unsafe extern "system" fn Java_rt_NativeRuntime_indexString<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>, utf8_str: JByteArray<'local>) -> jintArray {
    let graphemes = {
        let f_str = FearlessStr::new(&mut env, &utf8_str);
        let str = f_str.as_str();

        // Java ByteBuffers and byte[] must be indexable by a signed integer to be created,
        // so it is impossible to ever overflow when casting the usize to an i32
        str.grapheme_indices(true)
            .map(|(idx, _grapheme)| idx as jint)
            .collect::<Vec<_>>()
    };

    let res = env.new_int_array(graphemes.len() as jsize).unwrap();
    env.set_int_array_region(&res, 0, &graphemes).unwrap();
    res.into_raw()
}

#[no_mangle]
pub  extern "system" fn Java_rt_NativeRuntime_print<'local>(env: JNIEnv<'local>, _class: JClass<'local>, utf8_str: JByteArray<'local>) {
    print(env, utf8_str, false, io::stdout().lock());
}
#[no_mangle]
pub  extern "system" fn Java_rt_NativeRuntime_println<'local>(env: JNIEnv<'local>, _class: JClass<'local>, utf8_str: JByteArray<'local>) {
    print(env, utf8_str, true, io::stdout().lock());
}
#[no_mangle]
pub  extern "system" fn Java_rt_NativeRuntime_printErr<'local>(env: JNIEnv<'local>, _class: JClass<'local>, utf8_str: JByteArray<'local>) {
    print(env, utf8_str, false, io::stderr().lock());
}
#[no_mangle]
pub extern "system" fn Java_rt_NativeRuntime_printlnErr<'local>(env: JNIEnv<'local>, _class: JClass<'local>, utf8_str: JByteArray<'local>) {
    print(env, utf8_str, true, io::stderr().lock());
}

fn print<'local, B: Write>(mut env: JNIEnv<'local>, utf8_str: JByteArray<'local>, append_newline: bool, mut buffer: B) {
    let f_str = FearlessStr::new(&mut env, &utf8_str);
    let str = f_str.as_bytes();
    buffer.write_all(str).unwrap();
    if append_newline { buffer.write_all(b"\n").unwrap(); }
    buffer.flush().unwrap();
}
