package org.cloudbus.cloudsim.ex.geolocation.geoip2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.cloudbus.cloudsim.ex.util.CustomLog;
import org.cloudbus.cloudsim.ex.util.helpers.TestUtil;
import org.junit.BeforeClass;
import org.junit.Test;

public class GeoIP2IPGeneratorTest {

    private static File TEST_FILE = new File("test-resources/GeoIPCountryTest.csv");

    private static double NUM_AU_IPS_IN_TEST = 1788;
    private static double NUM_CN_IPS_IN_TEST = 59641;
    private static double NUM_JP_IPS_IN_TEST = 36861;
    @SuppressWarnings("unused")
    private static double NUM_TH_IPS_IN_TEST = 65534;

    private static final Set<String> COUNTRY_CODES = new HashSet<>(Arrays.asList("AU", "CN", "JP"));
    private static double IPS_IN_COUNTRY_CODES = NUM_AU_IPS_IN_TEST + NUM_CN_IPS_IN_TEST + NUM_JP_IPS_IN_TEST;

    private static GeoIP2IPGenerator generator;
    private static GeoIP2PingERService service;

    @BeforeClass
    public static void setUpClass() throws Exception {
	CustomLog.configLogger(TestUtil.LOG_PROPS);
	generator = new GeoIP2IPGenerator(COUNTRY_CODES, TEST_FILE, TestUtil.SEED);
	service = new GeoIP2PingERService(new File("GeoLite2-City.mmdb"), new File("PingTablePingER.tsv"), new File(
		"MonitoringSitesPingER.csv"));
    }

    @Test
    public void testIPGeneration() {
	final int TEST_SIZE = 10_000;
	final double DOUBLE_PRECISION = 0.01;

	Map<String, Integer> counts = new HashMap<>();
	for (String code : COUNTRY_CODES) {
	    counts.put(code, 0);
	}
	for (int i = 0; i < TEST_SIZE; i++) {
	    String ip = generator.pollRandomIP(service, -1);
	    String code = service.getMetaData(ip).getCountryIsoCode();
	    if (COUNTRY_CODES.contains(code)) {
		counts.put(code, counts.get(code) + 1);
	    } else {
		fail("IP " + Objects.toString(ip) + " is in location " + Objects.toString(code));
	    }
	}

	assertEquals(NUM_AU_IPS_IN_TEST / IPS_IN_COUNTRY_CODES, counts.get("AU") / (double) TEST_SIZE, DOUBLE_PRECISION);
	assertEquals(NUM_CN_IPS_IN_TEST / IPS_IN_COUNTRY_CODES, counts.get("CN") / (double) TEST_SIZE, DOUBLE_PRECISION);
	assertEquals(NUM_JP_IPS_IN_TEST / IPS_IN_COUNTRY_CODES, counts.get("JP") / (double) TEST_SIZE, DOUBLE_PRECISION);
	assertFalse(counts.containsKey("TH"));
    }
}
