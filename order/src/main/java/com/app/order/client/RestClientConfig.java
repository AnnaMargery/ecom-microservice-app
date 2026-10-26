package com.app.order.client;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

@Configuration
public class RestClientConfig {

	private final ObservationRegistry observationRegistry;
	private final Tracer tracer;
	private final Propagator propagator;

	public RestClientConfig(ObjectProvider<ObservationRegistry> observationRegistryProvider,
			ObjectProvider<Tracer> tracerProvider, ObjectProvider<Propagator> propagatorProvider) {
		this.observationRegistry = observationRegistryProvider.getIfAvailable(() -> ObservationRegistry.NOOP);
		this.tracer = tracerProvider.getIfAvailable(() -> Tracer.NOOP);
		this.propagator = propagatorProvider.getIfAvailable(() -> Propagator.NOOP);
	}

	@Bean
	@Primary
	public RestClient.Builder restClientBuilder() {
		return RestClient.builder();
	}

	@Bean
	@LoadBalanced
	public RestClient.Builder loadBalancedRestClientBuilder() {
		RestClient.Builder builder = RestClient.builder();

		if (nonNull(observationRegistry)) {
			builder.requestInterceptor(createTracingInterceptor());
		}
		return builder;
	}

	private ClientHttpRequestInterceptor createTracingInterceptor() {
		return ((request, body, execution) -> {
			if (nonNull(tracer) && nonNull(propagator) && nonNull(tracer.currentSpan())) {
				propagator.inject(requireNonNull(tracer.currentTraceContext().context()), request.getHeaders(),
						(carrier, key, value) -> carrier.add(key, value));
			}
			return execution.execute(request, body);
		});
	}

}
