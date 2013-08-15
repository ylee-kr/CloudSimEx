package org.cloudbus.cloudsim.ex.web.workload.sessions;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.cloudbus.cloudsim.ex.disk.DataItem;
import org.cloudbus.cloudsim.ex.web.CompositeGenerator;
import org.cloudbus.cloudsim.ex.web.IGenerator;
import org.cloudbus.cloudsim.ex.web.StatGenerator;
import org.cloudbus.cloudsim.ex.web.WebCloudlet;
import org.cloudbus.cloudsim.ex.web.WebSession;

public class StatSessionGenerator implements ISessionGenerator {

    private final Map<String, List<Double>> asSessionParams;
    private final Map<String, List<Double>> dbSessionParams;
    private final int userId;
    private final double idealLength;
    private final DataItem[] data;

    private Random dataRandomiser = new Random();

    public StatSessionGenerator(final Map<String, List<Double>> asSessionParams,
	    final Map<String, List<Double>> dbSessionParams,
	    final int userId, final int step, final DataItem... data) {
	super();
	this.asSessionParams = asSessionParams;
	this.dbSessionParams = dbSessionParams;
	this.userId = userId;
	this.data = data;

	this.idealLength = Math.max(Collections.max(asSessionParams.get("Time")),
		Collections.max(dbSessionParams.get("Time"))) + step;
    }

    public StatSessionGenerator(final Map<String, List<Double>> asSessionParams,
	    final Map<String, List<Double>> dbSessionParams,
	    final int userId, final int step, final long seed, final DataItem... data) {
	this(asSessionParams, dbSessionParams, userId, step, data);
	dataRandomiser.setSeed(seed);
    }

    @Override
    public WebSession generateSessionAt(final double time) {
	DataItem dataItem = pollRandomDataItem();

	final IGenerator<? extends WebCloudlet> appServerCloudLets = new StatGenerator(
		GeneratorsUtil.toGenerators(asSessionParams), dataItem);
	final IGenerator<? extends Collection<? extends WebCloudlet>> dbServerCloudLets = new CompositeGenerator<>(
		new StatGenerator(GeneratorsUtil.toGenerators(dbSessionParams), dataItem));

	int cloudletsNumber = asSessionParams.get(asSessionParams.keySet().toArray()[0]).size();
	return new WebSession(appServerCloudLets,
		dbServerCloudLets,
		userId,
		cloudletsNumber,
		time + idealLength);
    }

    private DataItem pollRandomDataItem() {
	DataItem dataItem = null;
	if (data == null || data.length == 0) {
	    dataItem = null;
	} else if (data.length == 1) {
	    dataItem = data[1];
	} else {
	    dataItem = data[dataRandomiser.nextInt(data.length)];
	}
	return dataItem;
    }
}
