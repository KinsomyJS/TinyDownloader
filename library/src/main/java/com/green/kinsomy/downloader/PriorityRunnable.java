package com.green.kinsomy.downloader;

/**
 * 带有优先级的Runnable类型
 * Created by kinsomy on 2018/4/11.
 */
class PriorityRunnable implements Runnable {

	public final Priority priority;//任务优先级
	private final Runnable runnable;//任务真正执行者
	long SEQ;//任务唯一标示

	public PriorityRunnable(Priority priority, Runnable runnable) {
		this.priority = priority == null ? Priority.NORMAL : priority;
		this.runnable = runnable;
	}

	@Override
	public final void run() {
		this.runnable.run();
	}
}