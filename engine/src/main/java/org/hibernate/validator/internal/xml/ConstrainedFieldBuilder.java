/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.internal.engine.valuehandling.UnwrapMode;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredField;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

/**
 * Builder for constraint fields.
 *
 * @author Hardy Ferentschik
 */
class ConstrainedFieldBuilder {
	private static final Log log = LoggerFactory.make();

	private ConstrainedFieldBuilder() {
	}

	static Set<ConstrainedField> buildConstrainedFields(List<FieldType> fields,
															   Class<?> beanClass,
															   String defaultPackage,
															   ConstraintHelper constraintHelper,
															   AnnotationProcessingOptionsImpl annotationProcessingOptions) {
		Set<ConstrainedField> constrainedFields = newHashSet();
		List<String> alreadyProcessedFieldNames = newArrayList();
		for ( FieldType fieldType : fields ) {
			Field field = findField( beanClass, fieldType.getName(), alreadyProcessedFieldNames );
			ConstraintLocation constraintLocation = ConstraintLocation.forProperty( field );
			Set<MetaConstraint<?>> metaConstraints = newHashSet();
			for ( ConstraintType constraint : fieldType.getConstraint() ) {
				MetaConstraint<?> metaConstraint = MetaConstraintBuilder.buildMetaConstraint(
						constraintLocation,
						constraint,
						java.lang.annotation.ElementType.FIELD,
						defaultPackage,
						constraintHelper,
						null
				);
				metaConstraints.add( metaConstraint );
			}
			Map<Class<?>, Class<?>> groupConversions = GroupConversionBuilder.buildGroupConversionMap(
					fieldType.getConvertGroup(),
					defaultPackage
			);

			// TODO HV-919 Support specification of type parameter constraints via XML and API
			ConstrainedField constrainedField = new ConstrainedField(
					ConfigurationSource.XML,
					constraintLocation,
					metaConstraints,
					Collections.<MetaConstraint<?>>emptySet(),
					groupConversions,
					fieldType.getValid() != null,
					UnwrapMode.AUTOMATIC
			);
			constrainedFields.add( constrainedField );


			// ignore annotations
			if ( fieldType.getIgnoreAnnotations() != null ) {
				annotationProcessingOptions.ignoreConstraintAnnotationsOnMember(
						field,
						fieldType.getIgnoreAnnotations()
				);
			}
		}

		return constrainedFields;
	}

	private static Field findField(Class<?> beanClass, String fieldName, List<String> alreadyProcessedFieldNames) {
		if ( alreadyProcessedFieldNames.contains( fieldName ) ) {
			throw log.getIsDefinedTwiceInMappingXmlForBeanException( fieldName, beanClass.getName() );
		}
		else {
			alreadyProcessedFieldNames.add( fieldName );
		}

		final Field field = run( GetDeclaredField.action( beanClass, fieldName ) );
		if ( field == null ) {
			throw log.getBeanDoesNotContainTheFieldException( beanClass.getName(), fieldName );
		}
		return field;
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private static <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
