# tree-sitter-fearless

A work in progress currently.

This parser follows the grammar of commit `0055528b388a`.

You need the `tree-sitter` CLI to compile this. For example, to run the playground you'd run:

```sh
tree-sitter build --wasm
tree-sitter playground
```

You can run tests like

```sh
tree-sitter test
```

## Regarding commits

If you edit `grammar.js`, make sure to regenerate the files in src with `tree-sitter generate`. I've been using the following git pre-commit hook:

```sh
#!/usr/bin/env sh
# Calls tree-sitter generate and adds the files iff grammar.js was staged
if git diff --name-only --cached | grep 'tree-sitter-fearless/grammar\.js' 1>/dev/null 2>/dev/null; then
	cd tree-sitter-fearless || exit 0
	tree-sitter generate || exit 1
	git add src
fi
```
