package org.jake.java;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.jake.utils.IterableUtils;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runners.model.TestClass;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class TestUtils {
	
	private static final String JUNIT4_RUNNER_CLASS_NAME = "org.junit.runner.JUnitCore";
	
	private static final String JUNIT3_RUNNER_CLASS_NAME = "junit.textui.TestRunner";
	
	private static final String JUNIT3_TEST_CASE_CLASS_NAME = "junit.framework.TestCase";
	
	private static final String JUNIT4_TEST_ANNOTATION_CLASS_NAME = "org.junit.Test";
	
	
	
	public static boolean isJunit4InClasspath(ClassLoader classLoader)  {
		try {
			classLoader.loadClass(JUNIT4_RUNNER_CLASS_NAME);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
	
	public static boolean isJunit3InClassPath(ClassLoader classLoader) {
		try {
			classLoader.loadClass(JUNIT3_RUNNER_CLASS_NAME);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
	
	
	public static int launchJunitTests(ClassLoader classLoader, File projectDir) {
		Collection<Class> classes = getJunitTestClassesInProject(classLoader, projectDir);
		
		if (isJunit4InClasspath(classLoader)) {
			Class[] classArray = IterableUtils.asArray(classes, Class.class);
			Result result = JUnitCore.runClasses(classArray);
			return result.getRunCount();
		
		} 
		if (isJunit3InClassPath(classLoader)) {
			int i = 0;
			for (Class clazz : classes) {
				i = i + TestRunner.run(new TestSuite(clazz)).runCount();
			}
			return i;
		}
		return 0;
	}
	
	public static Collection<Class> getJunitTestClassesInProject(ClassLoader classLoader, File projectDir) {
		File entry = ClasspathUtils.getClassEntryInsideProject(projectDir).get(0);
		Iterable<Class> classes = ClasspathUtils.getAllTopLevelClasses(classLoader, entry);
		List<Class> testClasses = new LinkedList<Class>();
		if (isJunit4InClasspath(classLoader)) {
			Class<Test> testAnnotation = load(classLoader, JUNIT4_TEST_ANNOTATION_CLASS_NAME);
			Class<TestCase> testCaseClass = load(classLoader, JUNIT3_TEST_CASE_CLASS_NAME);
			for (Class clazz : classes) {
				if (isJunit3Test(clazz, testCaseClass) || isJunit4Test(clazz, testAnnotation)) {
					testClasses.add(clazz);
				}
			}
		} else if (isJunit3InClassPath(classLoader)) {
			Class<TestCase> testCaseClass = load(classLoader, JUNIT3_TEST_CASE_CLASS_NAME);
			for (Class clazz : classes) {
				if (isJunit3Test(clazz, testCaseClass)) {
					testClasses.add(clazz);
				}
			}
		}
		return testClasses;
	}
	
	public static boolean isJunit3Test(Class candidtateClazz, Class<TestCase> testCaseClass) {
		if (Modifier.isAbstract(candidtateClazz.getModifiers())) {
			return false;
		}
		return testCaseClass.isAssignableFrom(candidtateClazz);
	}
	
	public static boolean isJunit4Test(Class candidateClass, Class<Test> testAnnotation) {
		if (Modifier.isAbstract(candidateClass.getModifiers())) {
			return false;
		}
		TestClass testClass = new TestClass(candidateClass);
		return !testClass.getAnnotatedMethods(testAnnotation).isEmpty();
	}
	
	private static <T> Class<T> load(ClassLoader classLoader, String name) {
		try {
			return (Class<T>) classLoader.loadClass(name);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	
	

}