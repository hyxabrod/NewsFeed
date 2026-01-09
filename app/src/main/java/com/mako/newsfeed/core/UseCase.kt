package com.mako.newsfeed.core

interface UseCase

interface UseCaseNoArgs : UseCase {
    suspend operator fun invoke(): PresentationEntity
}

interface UseCaseWithArgs<A : UseCaseWithArgs.Args> : UseCase {
    suspend operator fun invoke(args: A): PresentationEntity
    abstract class Args
}

interface ListUseCaseWithArgs<A : ListUseCaseWithArgs.Args> : UseCase {
    suspend operator fun invoke(args: A): List<PresentationEntity>
    abstract class Args
}

interface VoidUseCaseWithArgs<A : VoidUseCaseWithArgs.Args> : UseCase {
    suspend operator fun invoke(args: A)
    abstract class Args
}
