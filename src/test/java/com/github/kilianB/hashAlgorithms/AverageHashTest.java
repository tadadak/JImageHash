package com.github.kilianB.hashAlgorithms;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.kilianB.hashAlgorithms.AverageHash;
import com.github.kilianB.hashAlgorithms.HashingAlgorithm;
import com.github.kilianB.matcher.Hash;

class AverageHashTest {

	private static BufferedImage ballon;
	// Similar images
	private static BufferedImage copyright;
	private static BufferedImage highQuality;
	private static BufferedImage lowQuality;
	private static BufferedImage thumbnail;

	@BeforeAll
	static void loadImages() {
		try {
			ballon = ImageIO.read(AverageHashTest.class.getClassLoader().getResourceAsStream("ballon.jpg"));
			copyright = ImageIO.read(AverageHashTest.class.getClassLoader().getResourceAsStream("copyright.jpg"));
			highQuality = ImageIO
					.read(AverageHashTest.class.getClassLoader().getResourceAsStream("highQuality.jpg"));
			lowQuality = ImageIO.read(AverageHashTest.class.getClassLoader().getResourceAsStream("lowQuality.jpg"));
			thumbnail = ImageIO.read(AverageHashTest.class.getClassLoader().getResourceAsStream("thumbnail.jpg"));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Nested
	@DisplayName("Algorithm Id")
	class AlgorithmId {

		/**
		 * The algorithms id shall stay consistent throughout different instances of the
		 * jvm. While simple hashcodes do not guarantee this behaviour hash codes
		 * created from strings and integers are by contract consistent.
		 */
		@Test
		@DisplayName("Consistent AlgorithmIds")
		public void consistency() {

			assertAll(() -> {
				assertEquals(-363161106, new AverageHash(14).algorithmId());
			}, () -> {
				assertEquals(-363161095, new AverageHash(25).algorithmId());
			});
		}

		@Test
		@DisplayName("Unique AlgorithmsIds")
		public void uniquely() {

			int id0 = new AverageHash(2).algorithmId();
			int id1 = new AverageHash(14).algorithmId();
			int id2 = new AverageHash(25).algorithmId();

			assertAll(() -> {
				assertNotEquals(id0, id1);
			}, () -> {
				assertNotEquals(id0, id2);
			}, () -> {
				assertNotEquals(id1, id2);
			});

		}

	}

	@Test
	void keyLength() {
		// To get comparable hashes the key length has to be consistent for all
		// resolution of images

		AverageHash d1 = new AverageHash(32);

		Hash ballonHash = d1.hash(ballon);
		Hash copyrightHash = d1.hash(copyright);
		Hash lowQualityHash = d1.hash(lowQuality);
		Hash highQualityHash = d1.hash(highQuality);
		Hash thumbnailHash = d1.hash(thumbnail);

		assertAll(() -> {
			assertEquals(ballonHash.getHashValue().bitLength(), copyrightHash.getHashValue().bitLength());
		}, () -> {
			assertEquals(ballonHash.getHashValue().bitLength(), lowQualityHash.getHashValue().bitLength());
		}, () -> {
			assertEquals(ballonHash.getHashValue().bitLength(), highQualityHash.getHashValue().bitLength());
		}, () -> {
			assertEquals(ballonHash.getHashValue().bitLength(), thumbnailHash.getHashValue().bitLength());
		});

	}

	
	/**
	 * The hashes produced by the same algorithms shall return the same hash on sucessive 
	 * calls
	 * @param d1
	 */
	@ParameterizedTest
	@MethodSource(value = "algoInstances")
	void consitent(HashingAlgorithm d1) {
		assertEquals(d1.hash(ballon).getHashValue(), d1.hash(ballon).getHashValue());
	}

	/**
	 * The hemming distance of the same image has to be 0
	 * @deprecated not really a algorithm test case. Same as consistent
	 * @param d1
	 */
	@Deprecated
	@ParameterizedTest
	@MethodSource(value = "algoInstances")
	void equalImage(HashingAlgorithm d1) {
		assertEquals(0, d1.hash(ballon).hammingDistance(d1.hash(ballon)));
	}

	/**
	 * The hemming distance of similar images shall be lower than the distance of 
	 * vastly different picutres
	 * @param d1
	 */
	@ParameterizedTest
	@MethodSource(value = "algoInstances")
	void unequalImage(HashingAlgorithm d1) {
		Hash lowQualityHash = d1.hash(lowQuality);
		Hash highQualityHash = d1.hash(highQuality);
		Hash ballonHash = d1.hash(ballon);

		assertAll(() -> {
			assertTrue(lowQualityHash.hammingDistance(highQualityHash) < lowQualityHash.hammingDistance(ballonHash));
		}, () -> {
			assertTrue(highQualityHash.hammingDistance(lowQualityHash) < highQualityHash.hammingDistance(ballonHash));
		});
	}

	@SuppressWarnings("unused")
	private static Stream<HashingAlgorithm> algoInstances() {
		return Stream.of(new AverageHash(15), new AverageHash(20),
				new AverageHash(200));
	}
}