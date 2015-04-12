package org.jerkar.depmanagement;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jerkar.utils.JkUtilsFile;
import org.jerkar.utils.JkUtilsIterable;
import org.jerkar.utils.JkUtilsString;

public abstract class JkRepo {

	public static JkRepo ofOptional(String url, String userName, String password) {
		if (JkUtilsString.isBlank(url)) {
			return null;
		}
		return of(url).withCredential(userName, password);
	}

	public static JkRepo firstNonNull(JkRepo ...repos) {
		for (final JkRepo repo : repos) {
			if (repo != null) {
				return repo;
			}
		}
		return null;
	}

	public static MavenRepository maven(String url) {
		return new MavenRepository(toUrl(url), null, null);
	}

	public static MavenRepository maven(File file) {
		return new MavenRepository(JkUtilsFile.toUrl(file), null, null);
	}

	public static JkRepo mavenCentral() {
		return maven(MavenRepository.MAVEN_CENTRAL_URL.toString());
	}

	public static JkRepo mavenJCenter() {
		return maven(MavenRepository.JCENTERL_URL.toString());
	}

	public static JkRepo of(String url) {
		if (url.toLowerCase().startsWith("ivy:")) {
			return JkRepo.ivy(url.substring(4));
		}
		return JkRepo.maven(url);
	}

	public static JkRepo.IvyRepository ivy(URL url) {
		return new IvyRepository(url, null, null, null, null);
	}

	public static JkRepo.IvyRepository ivy(File file) {
		try {
			return ivy(file.toURI().toURL());
		} catch (final MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static JkRepo.IvyRepository ivy(String url) {
		try {
			return ivy(new URL(url));
		} catch (final MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private final URL url;

	private final String userName;

	private final String password;

	private JkRepo(URL url, String userName, String password) {
		this.url = url;
		this.userName = userName;
		this.password = password;
	}

	public final URL url() {
		return url;
	}

	public final String userName() {
		return userName;
	}

	public final String password() {
		return password;
	}

	public boolean hasCredentials() {
		return !JkUtilsString.isBlank(userName);
	}

	public final JkRepo withOptionalCredentials(String userName, String password) {
		if (JkUtilsString.isBlank(userName)) {
			return this;
		}
		return this.withCredential(userName, password);
	}

	public abstract JkRepo withCredential(String username, String password);

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final JkRepo other = (JkRepo) obj;
		if (url == null) {
			if (other.url != null) {
				return false;
			}
		} else if (!url.equals(other.url)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + url + ")";
	}

	private static URL toUrl(String url) {
		try {
			return new URL(url);
		} catch (final MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static final class MavenRepository extends JkRepo {

		public static final URL MAVEN_CENTRAL_URL = toUrl("http://repo1.maven.org/maven2");

		public static final URL JCENTERL_URL = toUrl("https://jcenter.bintray.com");

		private MavenRepository(URL url, String userName, String password) {
			super(url, userName, password);
		}

		@Override
		public JkRepo withCredential(String username, String password) {
			return new MavenRepository(this.url(), username, password);
		}



	}

	public static final class IvyRepository extends JkRepo {

		private final List<String> artifactPatterns;

		private final List<String> ivyPatterns;

		private static final String DEFAULT_IVY_ARTIFACT_PATTERN = "[organisation]/[module]/[type]s/[artifact]-[revision].[ext]";

		private static final String DEFAULT_IVY_IVY_PATTERN = "[organisation]/[module]/ivy-[revision].xml";

		private IvyRepository(URL url, String username, String password, List<String> artifactPatterns, List<String> ivyPatterns) {
			super(url, username, password);
			this.artifactPatterns = artifactPatterns;
			this.ivyPatterns = ivyPatterns;
		}

		public IvyRepository artifactPatterns(String ...patterns) {
			return new IvyRepository(this.url(), this.userName(), this.password(), Collections.unmodifiableList(Arrays.asList(patterns)), ivyPatterns);
		}

		public IvyRepository ivyPatterns(String ...patterns) {
			return new IvyRepository(this.url(), this.userName(), this.password(), artifactPatterns, Collections.unmodifiableList(Arrays.asList(patterns)));
		}

		public List<String> artifactPatterns() {
			if (this.artifactPatterns == null) {
				return JkUtilsIterable.listOf(DEFAULT_IVY_ARTIFACT_PATTERN);
			}
			return artifactPatterns;
		}

		public List<String> ivyPatterns() {
			if (this.ivyPatterns == null) {
				return JkUtilsIterable.listOf(DEFAULT_IVY_IVY_PATTERN);
			}
			return ivyPatterns;
		}

		@Override
		public JkRepo withCredential(String username, String password) {
			return new IvyRepository(this.url(), username, password, this.artifactPatterns, this.ivyPatterns);
		}
	}

}