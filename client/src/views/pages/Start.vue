<template>
  <section class="section">
    <div class="container">
      <ConnectionStatus
        :connected="connected && !checkingGame"
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

      <div class="tabs is-boxed is-large is-centered is-fullwidth" style="margin-top: 30px">
        <ul>
          <li :class="isTab('create') ? 'is-active' : ''">
            <a @click="tab = 'create'">
              <span class="icon is-small"><i class="fas fa-plus"></i></span>
              <span>Create new game</span>
            </a>
          </li>
          <li :class="isTab('join') ? 'is-active' : ''">
            <a @click="tab= 'join'">
              <span class="icon is-small"><i class="fas fa-people-group"></i></span>
              <span>Join existing game</span>
            </a>
          </li>
        </ul>
      </div>

      <div v-if="isTab('create')">
        <div class="field">
          <label class="label is-large">Nickname</label>
          <div class="control">
            <input
              :disabled="isAlreadyInAGame || checkingGame"
              @keyup.enter="handleKeyupEnter"
              :class="dangerIfExists(errors.nickname)"
              class="input is-large is-rounded"
              v-model="nickname"
              ref="nickname"
              placeholder="Choose a nickname..."
              maxlength="15"
            >
            <p
              class="help is-danger"
              v-if="errors.nickname"
            >
              {{ errors.nickname }}
            </p>
          </div>
        </div>

        <label class="label is-large">Rules</label>
        <div class="field is-grouped is-grouped-multiline">
          <div class="control">
            <div class="select is-rounded is-large">
              <select v-model="rules.numTrades" :disabled="isAlreadyInAGame || checkingGame">
                <option :value="2">2 trades</option>
                <option :value="3">3 trades</option>
              </select>
            </div>
          </div>

          <div class="control">
            <div class="select is-rounded is-large">
              <select v-model="rules.chicagoBestHand" :disabled="isAlreadyInAGame || checkingGame">
                <option :value="false">Chicago: All tricks</option>
                <option :value="true">Chicago: All tricks + Best hand</option>
              </select>
            </div>
          </div>

          <div class="control">
            <div class="select is-rounded is-large">
              <select v-model="rules.oneOpen" :disabled="isAlreadyInAGame || checkingGame">
                <option value="ALL">1 open: Anytime</option>
                <option value="FINAL">1 open: Final trade</option>
              </select>
            </div>
          </div>
        </div>

        <div class="field" style="margin-top: 18px">
          <div class="control">
            <button
              @click="createGame"
              :disabled="!hasNicknameSet || isAlreadyInAGame || checkingGame"
              class="button is-success is-rounded is-large"
            >
              Create new game
            </button>
          </div>
        </div>
      </div>

      <div v-if="isTab('join')">
        <div class="field">
          <label class="label is-large">Nickname</label>
          <div class="control">
            <input
              :disabled="isAlreadyInAGame || checkingGame"
              @keyup.enter="handleKeyupEnter"
              :class="dangerIfExists(errors.nickname)"
              class="input is-large is-rounded"
              v-model="nickname"
              ref="nickname"
              placeholder="Choose a nickname..."
              maxlength="15"
            >
            <p
              class="help is-danger"
              v-if="errors.nickname"
            >
              {{ errors.nickname }}
            </p>
          </div>
        </div>

        <div class="field">
          <label class="label is-large">Invitation key</label>
          <div class="control">
            <input
              :disabled="isAlreadyInAGame || checkingGame"
              :class="dangerIfExists(errors.invKey)"
              class="input is-large is-rounded"
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
        </div>

        <div class="field" style="margin-top: 30px">
          <div class="control">
            <a
              :disabled="!fieldsAreValid || isAlreadyInAGame || checkingGame"
              class="button is-info is-large is-rounded"
              @click="joinGame"
            >
              Join game
            </a>
          </div>
        </div>
      </div>

    </div>
  </section>
</template>
<script>
import { JOIN_GAME, NEW_GAME, CHECK_GAME } from '@/dto/action/types'
import { SEND_ACTION } from '@/store/modules/socket/action_types'
import JoinRequest from '@/dto/joinrequest/JoinRequest'
import GameCreation from '@/dto/gamecreation/GameCreation'
import RejoinRequest from '@/dto/rejoinrequest/RejoinRequest'
import Action from '@/dto/action/Action'
import ConnectionStatus from '@/views/components/ConnectionStatus'

export default {
  name: 'Start',
  components: {
    ConnectionStatus,
  },
  props: {
    currentlyInGame: {
      type: Boolean,
      default: undefined,
    },
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
      tab: 'create',
      invKey: '',
      nickname: '',
      leftGame: false,
      rules: {
        numTrades: 2,
        chicagoBestHand: false,
        oneOpen: 'ALL',
      }
    }
  },
  methods: {
    isTab(tab) {
      return this.tab === tab
    },
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
    rememberRules () {
      localStorage.setItem('rules', JSON.stringify(this.rules))
    },
    createGame () {
      if (!this.hasNicknameSet) return
      const gameCreation = new GameCreation(this.nickname, this.rules)
      const actionDTO = new Action(NEW_GAME, gameCreation)
      this.$emit('action', actionDTO)
      this.rememberNickname()
      this.rememberRules()
    },
    joinGame () {
      if (!this.fieldsAreValid) return
      const request = new JoinRequest(this.nickname, this.invKey)
      const actionDTO = new Action(JOIN_GAME, request)
      this.$emit('action', actionDTO)
      this.rememberNickname()
    },
    checkGame() {
      const storedInvitationKey = localStorage.getItem('invitationKey')
      const storedPlayerId = localStorage.getItem('playerId')

      if (storedInvitationKey && storedPlayerId) {
        const details = new RejoinRequest(storedPlayerId, storedInvitationKey)
        const actionDTO = new Action(CHECK_GAME, details)
        this.$store.dispatch(SEND_ACTION, actionDTO)
      } else {
        localStorage.removeItem('invitationKey')
        localStorage.removeItem('playerId')
        this.$store.commit('SET_CURRENTLY_IN_GAME', false) // yolo
      }
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
    checkingGame () {
      return this.currentlyInGame === undefined
    },
    isAlreadyInAGame () {
      return !this.leftGame && this.currentlyInGame
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
    const rulesJson = localStorage.getItem('rules')

    if (rulesJson) {
      this.rules = JSON.parse(rulesJson)
    }

    if (nickname) {
      this.nickname = nickname
    }
  },
  mounted() {
    if (this.invKey) this.tab = 'join'
    this.$refs.nickname.focus()
  },
  watch: {
    connected (conn) {
      if (conn) {
        this.checkGame()
      }
    },
    urlKey(key) {
      if (key) {
        this.tab = 'join'
      }
      this.invKey = key || this.invKey || ''
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
