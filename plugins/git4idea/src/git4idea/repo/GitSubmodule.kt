// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package git4idea.repo

class GitSubmodule(
  val repository: GitRepository,
  val parent: GitRepository)

fun GitRepository.asSubmodule() : GitSubmodule? {
  val repositoryManager = GitRepositoryManager.getInstance(project)
  val parent = repositoryManager.repositories.find { it.getDirectSubmodules().contains(this) }
  return if (parent != null) GitSubmodule(this, parent) else null
}

fun GitRepository.isSubmodule(): Boolean = asSubmodule() != null

fun GitRepository.getDirectSubmodules(): Collection<GitRepository> = GitRepositoryManager.getInstance(project).getDirectSubmodules(this)