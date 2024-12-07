pub mod lower;

use cranelift::prelude::*;
use cranelift_jit::{JITBuilder, JITModule};
use cranelift_module::Module;

pub struct Jit {
	pub builder_ctx: FunctionBuilderContext,
	/// The Cranelift codegen context. This is distinct per [Module] to allow
	/// for parallel compilation because this context is per-thread.
	pub ctx: codegen::Context,
	/// The context for data objects as opposed to functions.
	// pub data_description: DataDescription,
	pub module: JITModule,
}
impl Default for Jit {
	fn default() -> Self {
		// https://docs.wasmtime.dev/api/cranelift/prelude/settings/struct.Flags.html
		let mut flag_builder = settings::builder();
		flag_builder.set("use_colocated_libcalls", "false").unwrap();
		flag_builder.set("is_pic", "false").unwrap();
		flag_builder.set("opt_level", "speed").unwrap();
		flag_builder.set("enable_jump_tables", "true").unwrap();
		let isa_builder = cranelift_native::builder().unwrap_or_else(|msg| {
			panic!("host machine is not supported: {}", msg);
		});
		let isa = isa_builder
			.finish(settings::Flags::new(flag_builder))
			.unwrap();
		let builder = JITBuilder::with_isa(isa, cranelift_module::default_libcall_names());

		let module = JITModule::new(builder);
		Self {
			builder_ctx: FunctionBuilderContext::new(),
			ctx: module.make_context(),
			// data_description: DataDescription::new(),
			module,
		}
	}
}
