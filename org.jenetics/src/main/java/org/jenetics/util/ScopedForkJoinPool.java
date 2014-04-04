/*
 * Java Genetic Algorithm Library (@__identifier__@).
 * Copyright (c) @__year__@ Franz Wilhelmstötter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author:
 *    Franz Wilhelmstötter (franz.wilhelmstoetter@gmx.at)
 */
package org.jenetics.util;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

import org.jenetics.internal.util.Stack;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmx.at">Franz Wilhelmstötter</a>
 * @version 2.0 &mdash; <em>$Date: 2014-04-04 $</em>
 * @since 2.0
 */
final class ScopedForkJoinPool
	extends Concurrent
	implements Scoped<Concurrent>
{

	private final Stack<ForkJoinTask<?>> _tasks = new Stack<>();
	private final ForkJoinPool _pool;

	public ScopedForkJoinPool(final ForkJoinPool pool) {
		_pool = requireNonNull(pool);
	}

	@Override
	public void execute(final Runnable runnable) {
		_tasks.push(_pool.submit(runnable));
	}

	@Override
	public void execute(final List<? extends Runnable> runnables) {
		_tasks.push(_pool.submit(new RunnablesAction(runnables)));
	}

	@Override
	public Concurrent get() {
		return this;
	}

	@Override
	public void close() {
		for (ForkJoinTask<?> t = _tasks.pop(); t != null; t = _tasks.pop()) {
			t.join();
		}
	}
}
