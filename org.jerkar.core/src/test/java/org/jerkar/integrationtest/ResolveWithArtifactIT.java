package org.jerkar.integrationtest;

import org.jerkar.api.depmanagement.*;
import org.jerkar.api.utils.JkUtilsIterable;
import org.jerkar.api.utils.JkUtilsString;
import org.jerkar.tool.builtins.javabuild.JkJavaBuild;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Set;

import static org.jerkar.tool.builtins.javabuild.JkJavaBuild.*;
import static org.junit.Assert.*;

/**
 * Created by angibaudj on 19-06-17.
 */
public class ResolveWithArtifactIT {

    @Test
    public void artifactsAreHandled() {
        JkVersionedModule holder = JkVersionedModule.of("mygroup:myname", "myversion");
        JkDependencies deps = JkDependencies.builder()
                .usingDefaultScopes(JkJavaBuild.COMPILE)
                .on("org.lwjgl:lwjgl:3.1.1:natives-linux")
                .on(JkPopularModules.GUAVA, "19.0" )
                .on("org.lwjgl:lwjgl:3.1.1")
                .build();
        JkDependencyResolver resolver = JkDependencyResolver.managed(JkRepos.mavenCentral(), deps)
                .withParams(JkResolutionParameters.defaultScopeMapping(JkJavaBuild.DEFAULT_SCOPE_MAPPING))
                .withModuleHolder(holder);
        JkResolveResult resolveResult = resolver.resolve();
        JkDependencyNode treeRoot = resolveResult.dependencyTree();
        System.out.println(resolveResult.localFiles());
        System.out.println(treeRoot.toStringComplete());

        // Even if there is 2 declared dependencies on lwjgl, as it is the same module (with different artifact),
        // it should results in a single node.
        // The classpath order will also place all artifacts jump a same module subsequently
        assertEquals(2, treeRoot.children().size());
        assertEquals(3, treeRoot.allFiles().size());

        JkDependencyNode lwjglNode = treeRoot.children().get(0);
        List<File> lwjglFiles = lwjglNode.nodeInfo().files();
        System.out.println(lwjglFiles);
        assertEquals(2, lwjglFiles.size());

    }

    @Test
    public void artifactCountIsOk() {
        JkVersionedModule holder = JkVersionedModule.of("mygroup:myname", "myversion");
        JkDependencies deps = JkDependencies.builder()
                .on("org.springframework.boot:spring-boot-starter-web:1.5.3.RELEASE").scope(COMPILE_AND_RUNTIME)
                .on("org.springframework.boot:spring-boot-starter-test:1.5.+").scope(TEST)
                .on("com.github.briandilley.jsonrpc4j:jsonrpc4j:1.5.0").scope(COMPILE)
                .build();
        JkDependencyResolver resolver = JkDependencyResolver.managed(JkRepos.mavenCentral(), deps)
                .withParams(JkResolutionParameters.defaultScopeMapping(JkJavaBuild.DEFAULT_SCOPE_MAPPING))
                .withModuleHolder(holder);
        JkResolveResult resolveResult = resolver.resolve();
        JkDependencyNode tree = resolveResult.dependencyTree();
        System.out.println(tree.toStringComplete());
        System.out.println(JkUtilsString.join(resolveResult.localFiles(), "\n"));
        System.out.println("-----");
        System.out.println(JkUtilsString.join(tree.allFiles(), "\n"));
        //assertEquals(resolveResult.localFiles().size(), tree.allFiles().size());
        System.out.println("-----");
        for (File file : resolveResult.localFiles()) {
            if (!tree.allFiles().contains(file)) {
                System.out.println(file);
            }
        }

    }


    }
