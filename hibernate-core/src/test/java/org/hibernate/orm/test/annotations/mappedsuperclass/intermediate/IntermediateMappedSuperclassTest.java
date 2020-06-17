/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.annotations.mappedsuperclass.intermediate;

import java.math.BigDecimal;

import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * @author Steve Ebersole
 */
@DomainModel(
		annotatedClasses = {
				AccountBase.class, Account.class, SavingsAccountBase.class, SavingsAccount.class
		}
)
@SessionFactory
public class IntermediateMappedSuperclassTest {

	@AfterEach
	public void tearDown(SessionFactoryScope scope) {
		scope.inTransaction(
				session -> {
					session.createQuery( "from Account" ).list().forEach(
							account -> session.delete( account )
					);
				}
		);
	}

	@Test
	public void testGetOnIntermediateMappedSuperclass(SessionFactoryScope scope) {
		final BigDecimal withdrawalLimit = new BigDecimal( 1000.00 ).setScale( 2 );
		SavingsAccount savingsAccount = new SavingsAccount( "123", withdrawalLimit );
		scope.inTransaction(
				session -> {
					session.save( savingsAccount );
				}
		);

		scope.inTransaction(
				session -> {
					Account account = session.get( Account.class, savingsAccount.getId() );
					// Oracle returns the BigDecimal with scale=0, which is equal to 1000 (not 1000.00);
					// compare using BigDecimal.doubleValue;
					assertEquals(
							withdrawalLimit.doubleValue(),
							( (SavingsAccount) account ).getWithdrawalLimit().doubleValue(),
							0.001
					);
				}
		);
	}
}
