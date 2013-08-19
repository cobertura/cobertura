/**
 * 
 */
package net.sourceforge.cobertura.instrument;

import org.objectweb.asm.ClassWriter;

/**
 * @author schristou88
 *
 */
public class CoberturaClassWriter extends ClassWriter {
	public CoberturaClassWriter(final int flags) {
		super(flags);
	}

	@Override
	protected String getCommonSuperClass(final String type1, final String type2) {
		try {
			return super.getCommonSuperClass(type1, type2);
		} catch (RuntimeException e) {
			// Since the default super construction failed we need to dig further.
		}

		Class<?> c, d;
		// If system class fails to load, then let's use the auxClasspath url instead.
		try {
			c = Class.forName(type1.replace('/', '.'), false, ClassLoader
					.getSystemClassLoader());
		} catch (Exception e) {
			try {
				c = Class.forName(type1.replace('/', '.'), false,
						Main.urlClassLoader);
			} catch (Exception e1) {
				throw new RuntimeException(e1);
			}
		}

		// If system class fails to load, then let's use the auxClasspath url instead.
		try {
			d = Class.forName(type2.replace('/', '.'), false, ClassLoader
					.getSystemClassLoader());
		} catch (Exception e) {
			try {
				d = Class.forName(type2.replace('/', '.'), false,
						Main.urlClassLoader);
			} catch (Exception e1) {
				throw new RuntimeException(e1);
			}
		}

		if (c.isAssignableFrom(d)) {
			return type1;
		}
		if (d.isAssignableFrom(c)) {
			return type2;
		}
		if (c.isInterface() || d.isInterface()) {
			return "java/lang/Object";
		} else {
			do {
				c = c.getSuperclass();
			} while (!c.isAssignableFrom(d));
			return c.getName().replace('.', '/');
		}
	}
}
