package program;

import astFull.Package;
import codegen.MIR;
import org.antlr.v4.runtime.RuleContext;
import utils.Bug;
import utils.Mapper;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record CompilationCache(String pkg, int cacheKey, List<MIR.Trait> compiled) implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  public static Map<String, CompilationCache> getOkPkgs(List<Package> ps) {
    return Mapper.of(map->ps
      .forEach(pkg->load(pkg).ifPresent(cache->map.put(pkg.name(), cache))));
  }

  public static Optional<CompilationCache> load(Package pkg) {
    var path = getPath(cacheKey(pkg));
    if (!path.exists() || !path.canRead()) { return Optional.empty(); }
    try (var in = new ObjectInputStream(new FileInputStream(path))) {
      return Optional.of((CompilationCache) in.readObject());
    } catch (ClassNotFoundException | ClassCastException e) {
      System.err.println("Warning: Error deserialising the compilation cache: "+e);
      return Optional.empty();
    } catch (IOException err) {
      System.err.println("Warning: Error reading the compilation cache: "+err);
      return Optional.empty();
    }
  }

  public CompilationCache(Package pkg, List<MIR.Trait> compiled) {
    this(pkg.name(), cacheKey(pkg), compiled);
  }

  public void save() {
    try (var out = new ObjectOutputStream(new FileOutputStream(getPath(cacheKey)))) {
      out.writeObject(this);
      out.flush();
    } catch (NotSerializableException err) {
      getPath(cacheKey).delete();
      throw Bug.of(err);
    } catch (IOException err) {
      System.err.println("Warning: Error writing to the compilation cache: "+err);
    }
  }

  private static File getPath(int cacheKey) {
    var workingPath = Paths.get(System.getProperty("java.io.tmpdir"), "fearless_tmp");
    var workingDir = workingPath.toFile();
    if (!workingDir.exists()) {
      workingDir.mkdirs();
    }
    return workingPath.resolve((cacheKey+"").replace('-', 'X')).toFile();
  }

  private static int cacheKey(Package pkg) {
    return pkg.ds().stream().map(RuleContext::getText).toList().hashCode();
  }
}
