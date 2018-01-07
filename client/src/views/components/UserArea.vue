<template>
  <div class="media">
    <div class="media-left">
      <figure class="image is-96x96">
        <img :src="getAvatarUrl(user)" class="avatar">
      </figure>
    </div>
    <div class="media-content" style="max-height: 96px">
      <h1 class="title" style="margin-bottom: 1rem">{{ user.name || 'You' }}</h1>
      <h2 class="title is-3">
        <span class="tag is-large">{{ user.score }} points</span>
        <span class="tag is-large" v-if="user.hasTakenChicago">C</span>
        <span class="tag is-large" v-if="user.winner">WINNER</span>
      </h2>
    </div>
  </div>
</template>
<script>
export default {
  name: 'UserArea',
  props: ['user']
}
</script>
