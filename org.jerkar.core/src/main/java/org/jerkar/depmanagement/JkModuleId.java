package org.jerkar.depmanagement;

import org.jerkar.utils.JkUtilsString;

/**
 * Identifier for a module. Compound of a group name and a name.
 * Used to identify module inside a repository.
 * Ex : group='org.hibernate' name='hibernate-core'.
 * 
 * @author Jerome Angibaud
 */
public final class JkModuleId {

	/**
	 * Creates a <code>JkModuleId</code> from a group and a name.
	 */
	public static JkModuleId of(String group, String name) {
		return new JkModuleId(group, name);
	}

	/**
	 * Creates a <code>JkModuleId</code> from a string formed as 'group:name'.
	 */
	public static JkModuleId of(String groupAndName) {
		final String strings[] = JkUtilsString.split(groupAndName, ":");
		if (strings.length != 2) {
			throw new IllegalArgumentException("Module should be formated as 'groupName:moduleName'. Was " + groupAndName + ".");
		}
		return of(strings[0], strings[1]);
	}

	private final String group;

	private final String name;

	private JkModuleId(String organisation, String name) {
		super();
		this.group = organisation;
		this.name = name;
	}

	public String group() {
		return group;
	}

	public String name() {
		return name;
	}

	@Override
	public String toString() {
		return group+":"+name;

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		final JkModuleId other = (JkModuleId) obj;
		if (group == null) {
			if (other.group != null) {
				return false;
			}
		} else if (!group.equals(other.group)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}



}