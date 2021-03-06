/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.messageinterpolation;

import java.util.Locale;

import org.hibernate.validator.internal.engine.messageinterpolation.InterpolationTerm;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;

/**
 * Resource bundle backed message interpolator.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Adam Stawicki
 */
public class ResourceBundleMessageInterpolator extends AbstractMessageInterpolator {
	private static final Log log = LoggerFactory.make();
	
	public ResourceBundleMessageInterpolator(ResourceBundleLocator defaultResourceBundleLocator) {
		this(defaultResourceBundleLocator, true);
	}

	public ResourceBundleMessageInterpolator() {
		super();
	}

	public ResourceBundleMessageInterpolator(ResourceBundleLocator testResourceBundleLocator, boolean cachingEnabled) {
		super(testResourceBundleLocator, cachingEnabled);
	}

	@Override
	public String interpolate(Context context, Locale locale, String term) {
		InterpolationTerm expression = new InterpolationTerm( term, locale );
		return expression.interpolate( context );
	}
	
	
}
