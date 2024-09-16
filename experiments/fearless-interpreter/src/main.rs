use capnp::message::{ReaderOptions, TypedReader};

mod proto;
mod schema_capnp;

fn main() {
    let f = std::fs::read("/tmp/test.fear.pkg.mearless").unwrap();
    let reader = capnp::serialize::read_message_from_flat_slice(
        &mut &*f,
        ReaderOptions::new()
    ).unwrap();
    let reader = TypedReader::<_, schema_capnp::package::Owned>::new(reader);
    let pkg_reader = reader.get().unwrap();
    let pkg_name = std::str::from_utf8(pkg_reader.get_name().unwrap().0).unwrap();
    println!("Hello, world! {}", pkg_name);
}
