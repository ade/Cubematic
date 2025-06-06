package se.ade.mc.cubematic.progression.analysis

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import se.ade.mc.cubematic.progression.analysis.key.NodeKey

suspend fun deriveObtainableItems(
	graph: DependencyGraph,
	startWith: Set<NodeKey>,
): Set<Derived> {
	val queue = mutableSetOf<NodeKey>()
	val unlocked = mutableMapOf<NodeKey, Derived>()
	val scope = CoroutineScope(Dispatchers.Default)

	startWith.forEach {
		unlocked[it] = Derived(it)
	}
	queue.addAll(graph.nodes.map { it.id } - unlocked.keys)

	do {
		val jobs = queue.map { it ->
			scope.async { deriveOrNull(graph, it, unlocked.keys) }
		}
		val scrape = jobs.mapNotNull { it.await() }
		scrape.forEach { derived ->
			unlocked[derived.nodeKey] = derived
			queue.remove(derived.nodeKey)
		}
	} while (scrape.isNotEmpty())

	return unlocked.values.toSet()
}

fun deriveOrNull(
	graph: DependencyGraph,
	derivingKey: NodeKey,
	unlocked: Set<NodeKey>,
	visited: List<NodeKey> = listOf<NodeKey>()
): Derived? {
	val item = graph.nodes.firstOrNull {
		it.id == derivingKey
	} ?: return null

	// Cyclic dependency check prevention.
	if(visited.contains(item.id))
		return null

	val nextVisited = visited + item.id

	val validSourceNodes = item.sources.flatMap { source ->
		source.transforms.mapNotNull { transform ->
			val required = transform.input + transform.tools

			// For each requirement, get a dependency chain of what supplies it
			val dependencyChains = required.map { req ->
				when(req) {
					is ProcessRequirement.Single -> {
						if(req.key in unlocked) {
							Derived(req.key)
						} else {
							deriveOrNull(graph, req.key, unlocked, nextVisited)
						}
					}
					is ProcessRequirement.Any -> {
						val have = req.filter.anyOf.firstOrNull { unlocked.contains(it) }
						if(have != null)
							Derived(have)
						else
							req.filter.anyOf.firstNotNullOfOrNull {
								deriveOrNull(graph, it, unlocked, nextVisited)
							}
					}
				}
			}

			dependencyChains
				.takeIf { it.none { it == null } }
				?.filterNotNull()
		}
	}

	return if(validSourceNodes.isNotEmpty()) {
		Derived(derivingKey, validSourceNodes)
	} else {
		// No valid source found, so this item is not derivable in this pass
		null
	}
}

data class Derived(
	val nodeKey: NodeKey,
	val from: List<List<Derived>> = emptyList(),
) {
	fun trace() = StringBuilder().also {
		trace(it)
	}.toString()

	private fun trace(sb: StringBuilder, depth: Int = 0) {
		val indent = " ".repeat(depth * 2)
		sb.append("$indent$nodeKey\n")
		from.forEachIndexed { index, alternative ->
			if(from.size > 1)
				sb.append("$indent  ---[${index+1}/${from.size}: $nodeKey]---\n")

			alternative.forEach { it ->
				it.trace(sb, depth + 1)
			}
		}
	}
}