<template>
  <div class="home">
    <h1 class="title">AI Agent</h1>
    <p class="subtitle">请选择一个 AI 应用</p>
    <div class="card-list">
      <div class="card" @click="$router.push('/love-master')">
        <div class="card-icon">❤️</div>
        <div class="card-info">
          <h2>AI 智能恋爱大师</h2>
          <p>你的私人恋爱顾问，帮你解答情感问题</p>
        </div>
        <div class="card-arrow">→</div>
      </div>
      <div class="card" @click="$router.push('/manus')">
        <div class="card-icon">🤖</div>
        <div class="card-info">
          <h2>AI 超级智能体</h2>
          <p>强大的 AI 智能体，帮你完成各种任务</p>
        </div>
        <div class="card-arrow">→</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home {
  max-width: 600px;
  margin: 0 auto;
  padding: 60px 20px;
}

.title {
  text-align: center;
  font-size: 32px;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.subtitle {
  text-align: center;
  color: #888;
  margin-bottom: 40px;
  font-size: 16px;
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card {
  display: flex;
  align-items: center;
  padding: 20px 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.card-icon {
  font-size: 36px;
  margin-right: 16px;
}

.card-info {
  flex: 1;
}

.card-info h2 {
  font-size: 18px;
  color: #1a1a2e;
  margin: 0 0 4px 0;
}

.card-info p {
  font-size: 14px;
  color: #888;
  margin: 0;
}

.card-arrow {
  font-size: 20px;
  color: #ccc;
}
</style>