# English Word Quiz

Spring Boot + Thymeleaf + JPA で作成した英単語クイズアプリです。

## 機能
- 単語登録 / 編集 / 検索
- 品詞絞り込み
- カンマ区切りで複数意味の登録
- クイズ形式で出題・正誤判定

## TODO
- [ ] クイズ出題で「単語登録が０件」の場合の画面遷移・エラーメッセージ
- [ ] 「部分結果」→「次へ」ボタンで正しく次の問題へ遷移させる
- [ ] テストコード（JUnit + Mockito）を追加
- [ ] GitHub Actions でCI／自動デプロイを設定
- [ ] サジェスト付き検索（Typeahead.js など）
- [ ] Dockerize & `docker-compose` でDB付き起動
- [ ] データベースの永続化

## 動かし方

```bash
# クローン
git clone https://github.com/your-username/english-word-quiz-final.git
cd english-word-quiz-final

# 起動 (内部で H2 をメモリ起動)
mvn spring-boot:run

# ブラウザで → http://localhost:8080/app/menu
