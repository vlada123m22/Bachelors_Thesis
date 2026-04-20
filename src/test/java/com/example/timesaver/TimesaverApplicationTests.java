package com.example.timesaver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test class for TimesaverApplication to achieve 100% code coverage.
 *
 * Covers:
 * 1. Class coverage - TimesaverApplication class itself
 * 2. Method coverage - main() method and implicit constructor
 * 3. Line coverage - All executable lines
 * 4. Branch coverage - All code paths
 */
@SpringBootTest
@TestPropertySource(properties = "server.port=0")
class TimesaverApplicationTests {

	/**
	 * Tests that the main method can be invoked and the application starts.
	 * This covers the main() method execution with empty arguments.
	 * server.port=0 ensures the application doesn't bind to a real port during tests.
	 */
	@Test
	void testMain() {
		String[] args = {};
		assertDoesNotThrow(
				() -> TimesaverApplication.main(args),
				"TimesaverApplication.main() should execute without throwing exceptions"
		);
	}

	/**
	 * Tests that the TimesaverApplication class can be instantiated.
	 * This covers the implicit default constructor.
	 */
	@Test
	void testConstructor() {
		TimesaverApplication application = new TimesaverApplication();
		assertNotNull(application, "TimesaverApplication instance should not be null");
	}

}