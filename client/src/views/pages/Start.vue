<template>
  <section class="section">
        <div class="container">
          <ConnectionStatus
            :connected="connected"
          />
          <section
            v-if="isAlreadyInAGame"
            class="hero is-warning"
            style="margin-top: 30px"
          >
            <div class="hero-body">
              <p class="title">
                You're currently in a game
              </p>
              <p class="subtitle">
                Do you want to rejoin?
              </p>
              <p>
                <a class="button is-black" @click="rejoin">Yes, rejoin</a>
                <a class="button is-warning is-light" @click="leave">No, leave game</a>
              </p>
            </div>
          </section>
          <div class="field" style="padding-top: 30px">
            <div class="control">
              <input
                :disabled="isAlreadyInAGame"
                @keyup.enter="handleKeyupEnter"
                :class="dangerIfExists(errors.nickname)"
                class="input is-large"
                v-model="nickname"
                ref="nickname"
                placeholder="Choose a nickname..."
              >
              <p
              class="help is-danger"
              v-if="errors.nickname">
                {{ errors.nickname }}
              </p>
            </div>
          </div>
          <div class="columns">
            <div class="column is-expanded">
                <div class="field has-addons is-fullwidth">
                  <div class="control is-expanded">
                    <input
                      :disabled="isAlreadyInAGame"
                      :class="dangerIfExists(errors.invKey)"
                      class="input is-large"
                      type="text"
                      placeholder="Invitation key"
                      v-model="invKey"
                    >
                      <p
                        class="help is-danger"
                        v-if="errors.invKey"
                      >
                        {{ errors.invKey }}
                      </p>
                  </div>
                  <div class="control">
                    <a
                      :disabled="!fieldsAreValid || isAlreadyInAGame"
                      class="button is-info is-large"
                      @click="joinGame"
                    >
                      Join existing game
                    </a>
                  </div>
                </div>
              </div>
              <div class="column is-narrow">
                <button
                class="button is-primary is-large"
                @click="createGame"
                :disabled="!hasNicknameSet || isAlreadyInAGame">
                  Create new game
                </button>
              </div>
          </div>
        </div>
      </section>
</template>
<script>
import { JOIN_GAME, NEW_GAME } from '@/dto/action/types'
import JoinRequest from '@/dto/joinrequest/JoinRequest'
import Action from '@/dto/action/Action'
import ConnectionStatus from '@/views/components/ConnectionStatus'

export default {
  name: 'Start',
  components: {
    ConnectionStatus,
  },
  props: {
    connected: {
      type: Boolean,
      required: true,
    },
    urlKey: {
      type: String,
      default: ''
    },
    errors: {
      type: Object,
      required: true
    }
  },
  data: () => {
    return {
      invKey: '',
      nickname: '',
      leftGame: false,
    }
  },
  methods: {
    rejoin() {
      this.$emit('rejoin')
    },
    leave() {
      this.$emit('leave')
      this.leftGame = true
    },
    rememberNickname () {
      if (this.nickname) {
        localStorage.setItem('nickname', this.nickname)
      }
    },
    createGame () {
      if (!this.hasNicknameSet) return
      const actionDTO = new Action(NEW_GAME, this.nickname)
      this.$emit('action', actionDTO)
      this.rememberNickname()
    },
    joinGame () {
      if (!this.fieldsAreValid) return
      const request = new JoinRequest(this.nickname, this.invKey)
      const actionDTO = new Action(JOIN_GAME, request)
      this.$emit('action', actionDTO)
      this.rememberNickname()
    },
    dangerIfExists (value) {
      return this[value] ? 'is-danger' : ''
    },
    handleKeyupEnter () {
      if (this.invKey) {
        this.joinGame()
      } else {
        this.createGame()
      }
    }
  },
  computed: {
    isAlreadyInAGame () {
      const storedInvitationKey = localStorage.getItem('invitationKey')
      const storedPlayerId = localStorage.getItem('invitationKey')
      return !this.leftGame && Boolean(storedInvitationKey && storedPlayerId)
    },
    hasNicknameSet () {
      return this.nickname.length > 0
    },
    hasKeySet () {
      const sections = this.invKey.split('-')
      if (sections.length === 5) {
        for (let section of sections) {
          if (section === '') return false
        }
        return true
      }
      return false
    },
    hasErrors () {
      return this.errors.invKey.length > 0 ||
        this.errors.nickname.length > 0
    },
    fieldsAreValid () {
      return !this.hasErrors && this.hasNicknameSet && this.hasKeySet
    }
  },
  created () {
    this.invKey = this.urlKey || ''

    const nickname = localStorage.getItem('nickname')
    if (nickname) {
      this.nickname = nickname
    }
  },
  mounted() {
    this.$refs.nickname.focus()
  },
  watch: {
    urlKey() {
      this.invKey = this.urlKey || this.invKey || ''
    },
    invKey () {
      this.$emit('changeKey')
    },
    nickname () {
      this.$emit('changeNick')
    }
  }
}
</script>

<style lang="scss">
</style>
