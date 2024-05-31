package tree_sitter_fearless_test

import (
	"testing"

	tree_sitter "github.com/smacker/go-tree-sitter"
	"github.com/tree-sitter/tree-sitter-fearless"
)

func TestCanLoadGrammar(t *testing.T) {
	language := tree_sitter.NewLanguage(tree_sitter_fearless.Language())
	if language == nil {
		t.Errorf("Error loading Fearless grammar")
	}
}
