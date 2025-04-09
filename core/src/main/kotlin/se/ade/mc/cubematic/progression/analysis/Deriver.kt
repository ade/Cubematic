package se.ade.mc.cubematic.progression.analysis

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.bukkit.Material
import se.ade.mc.cubematic.progression.analysis.key.NodeKey

suspend fun deriveObtainableItems(
	graph: DependencyGraph,
	startWith: Set<NodeKey>,
): Set<NodeKey> {
	val queue = mutableSetOf<NodeKey>()
	val unlocked = mutableSetOf<NodeKey>()
	val scope = CoroutineScope(Dispatchers.Default)

	unlocked.addAll(startWith)
	queue.addAll(graph.nodes.map { it.id } - unlocked)

	do {
		val jobs = queue.map { it ->
			scope.async { deriveSingleOrNull(graph, it, unlocked) }
		}
		val results = jobs.mapNotNull { it.await() }
		results.forEach { result ->
			unlocked.add(result)
			queue.remove(result)
		}
	} while (results.isNotEmpty())

	return unlocked
}

fun deriveSingleOrNull(
	graph: DependencyGraph,
	derivingKey: NodeKey,
	unlocked: Set<NodeKey>,
	visited: Set<NodeKey> = setOf<NodeKey>()
): NodeKey? {
	val item = graph.nodes.firstOrNull {
		it.id == derivingKey
	} ?: return null

	// Cyclic dependency check prevention.
	if(visited.contains(item.id))
		return null

	val nextVisited = visited + item.id

	val derivable = item.sources.any { source ->
		source.transforms.any { transform ->
			val required = transform.input + transform.tools
			required.all { req ->
				when(req) {
					is ProcessRequirement.Type -> {
						req.key in unlocked
								|| deriveSingleOrNull(graph, req.key, unlocked, nextVisited) != null
					}
					is ProcessRequirement.Any -> {
						val hasAlready = unlocked.any {
							req.filter.anyOf.contains(it)
						}

						hasAlready || req.filter.anyOf.any {
							deriveSingleOrNull(graph, it, unlocked, nextVisited) != null
						}
					}
					is ProcessRequirement.Mechanic -> {
						// Only way to satisfy this is to have the mechanic already.
						req.mechanic.key in unlocked
					}
				}
			}
		}
	}

	return if(derivable) {
		item.id
	} else {
		null
	}
}
