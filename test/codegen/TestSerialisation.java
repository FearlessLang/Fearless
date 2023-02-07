package codegen;

import ast.T;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import id.Id;
import id.Mdf;
import main.Main;
import org.junit.jupiter.api.Test;
import utils.Err;

import java.util.List;
import java.util.Optional;

public class TestSerialisation {
  void ok(String expected, List<E.Dec> ds) {
    Main.resetAll();
    var toJson = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    try {
      Err.strCmp(expected, toJson.writeValueAsString(ds));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Test void emptyProgram() { ok("[ ]", List.of()); }
  @Test void simpleProgram() { ok("""
    [ {
      "name" : "MyApp",
      "gxs" : [ ],
      "lambda" : {
        "mdf" : "imm",
        "its" : [ {
          "name" : {
            "name" : "Main",
            "gen" : 0
          },
          "ts" : [ ]
        } ],
        "selfName" : "this",
        "meths" : [ {
          "mdf" : "imm",
          "gens" : [ ],
          "ts" : [ {
            "mdf" : "lent",
            "rt" : {
              "name" : {
                "name" : "System",
                "gen" : 0
              },
              "ts" : [ ]
            },
            "it" : true
          } ],
          "xs" : [ "s" ],
          "ret" : {
            "mdf" : "imm",
            "rt" : {
              "name" : {
                "name" : "Void",
                "gen" : 0
              },
              "ts" : [ ]
            },
            "it" : true
          },
          "name" : "#",
          "body" : {
            "empty" : false,
            "present" : true
          }
        } ],
        "captures" : [ ]
      }
    } ]
    """, List.of(
    new E.Dec("MyApp", List.of(), new E.Lambda(
      Mdf.imm,
      List.of(new Id.IT<T>("Main", List.of())),
      "this",
      List.of(new E.Meth(
        Mdf.imm,
        List.of(),
        List.of(new T(Mdf.lent, new Id.IT<T>("System", List.of()))),
        List.of("s"),
        new T(Mdf.imm, new Id.IT<>("Void", List.of())),
        "#",
        Optional.of(new E.MCall(new E.X("s"), ".print", List.of(new E.Lambda(
          Mdf.imm,
          List.of(new Id.IT<>("\"Hello, World!\"", List.of())),
          "fear0$",
          List.of(),
          List.of()
        ))))
      )),
      List.of()
    ))
  )); }
}
