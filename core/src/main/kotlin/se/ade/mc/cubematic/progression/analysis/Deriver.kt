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
			scope.async { deriveOrNull(graph, it, unlocked.keys.toSet()) }
		}
		val derivations = jobs.mapNotNull { it.await() }
		derivations.forEach { derived ->
			unlocked[derived.nodeKey] = derived
			queue.remove(derived.nodeKey)
		}
	} while (derivations.isNotEmpty())

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

	val validSourceNodes = item.sources.firstNotNullOfOrNull { source ->
		source.transforms.firstNotNullOfOrNull { transform ->
			val required = transform.input + transform.tools

			// For each requirement, get a dependency chain of what supplies it
			val dependencyChains = required.map { req ->
				when(req) {
					is ProcessRequirement.Type -> {
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
					is ProcessRequirement.Mechanic -> {
						// Only way to satisfy this is to have the mechanic already.
						if(req.mechanic.key in unlocked)
							Derived(req.mechanic.key)
						else
							null
					}
				}
			}

			dependencyChains
				.takeIf { it.none { it == null } }
				?.filterNotNull()
		}
	}

	return if(validSourceNodes != null) {
		Derived(derivingKey, validSourceNodes)
	} else {
		// No valid source found, so this item is not derivable in this pass
		null
	}
}

data class Derived(
	val nodeKey: NodeKey,
	val from: List<Derived> = emptyList(),
) {
	fun print(depth: Int = 0) {
		val indent = " ".repeat(depth * 2)
		println("$indent$nodeKey")
		from.forEach { it.print(depth + 1) }
	}

	fun trace() = StringBuilder().also {
		trace(it)
	}.toString()

	private fun trace(sb: StringBuilder, depth: Int = 0) {
		val indent = " ".repeat(depth * 2)
		sb.append("$indent$nodeKey\n")
		from.forEach { it.trace(sb, depth + 1) }
	}
}