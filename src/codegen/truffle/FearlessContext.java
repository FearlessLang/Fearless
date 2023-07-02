package codegen.truffle;

import id.Id;

import java.util.Map;

public record FearlessContext(Map<Id.DecId, FearlessTrait> ts) {
}
