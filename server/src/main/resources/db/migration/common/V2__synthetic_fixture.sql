INSERT INTO brand (id, name) VALUES
('10000000-0000-0000-0000-000000000001', 'Atlas Research Fixtures');

INSERT INTO tag (id, slug, label_pt, label_en, tag_group) VALUES
('20000000-0000-0000-0000-000000000001', 'antidepressant', 'Antidepressivo', 'Antidepressant', 'THERAPEUTIC_CLASS'),
('20000000-0000-0000-0000-000000000002', 'stimulant', 'Estimulante', 'Stimulant', 'PHYSIOLOGICAL_EFFECT'),
('20000000-0000-0000-0000-000000000003', 'anabolic', 'Anabolizante', 'Anabolic', 'PHYSIOLOGICAL_EFFECT'),
('20000000-0000-0000-0000-000000000004', 'glp1-agonist', 'Agonista de GLP-1', 'GLP-1 agonist', 'MECHANISM'),
('20000000-0000-0000-0000-000000000005', 'experimental-peptide', 'Peptídeo experimental', 'Experimental peptide', 'REGULATORY_STATUS'),
('20000000-0000-0000-0000-000000000006', 'dependency-risk', 'Risco de dependência', 'Dependency risk', 'RISK');

INSERT INTO substance (id, canonical_name, description_pt, description_en, efficacy_summary_score, risk_overall_score, evidence_level, review_status, published_revision) VALUES
('30000000-0000-0000-0000-000000000001', 'Atlasine', 'Exemplo sintético de um composto com evidência moderada. Não representa uma substância real.', 'Synthetic example of a compound with moderate evidence. It does not represent a real substance.', 6, 3, 'MODERATE', 'APPROVED', 1),
('30000000-0000-0000-0000-000000000002', 'Neruvex', 'Exemplo sintético com benefício pequeno e risco relevante.', 'Synthetic example with small benefit and relevant risk.', 4, 6, 'LOW', 'APPROVED', 1),
('30000000-0000-0000-0000-000000000003', 'Metabryl', 'Exemplo sintético de mecanismo metabólico em revisão.', 'Synthetic metabolic-mechanism example under review.', NULL, 4, 'INSUFFICIENT', 'IN_REVIEW', 0),
('30000000-0000-0000-0000-000000000004', 'Peptilon-X', 'Peptídeo inteiramente fictício usado para demonstrar incerteza regulatória.', 'Entirely fictional peptide used to demonstrate regulatory uncertainty.', NULL, NULL, 'INSUFFICIENT', 'DRAFT', 0),
('30000000-0000-0000-0000-000000000005', 'Vigorane', 'Exemplo sintético anabólico com perfil de alto risco.', 'Synthetic anabolic example with a high-risk profile.', 7, 8, 'LOW', 'APPROVED', 1),
('30000000-0000-0000-0000-000000000006', 'Calmora', 'Exemplo sintético antidepressivo com evidência de alta qualidade.', 'Synthetic antidepressant example with high-quality evidence.', 8, 4, 'HIGH', 'APPROVED', 2);

INSERT INTO substance_alias (id, substance_id, locale, alias, normalization_source) VALUES
('31000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'pt-BR', 'Atlasina', 'synthetic-fixture'),
('31000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000004', 'en', 'PX', 'synthetic-fixture');

INSERT INTO substance_tag (substance_id, tag_id) VALUES
('30000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002'),
('30000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000004'),
('30000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000005'),
('30000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000003'),
('30000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000001'),
('30000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000006');

INSERT INTO product (id, brand_id, original_name, store_category, observed_price, currency, listing_url, captured_at, parse_status) VALUES
('40000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'Atlasine Research Reference', 'Synthetic fixtures', 10.00, 'BRL', NULL, CURRENT_TIMESTAMP, 'PARSED'),
('40000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', 'Vigorane Demonstration Product', 'Synthetic fixtures', 20.00, 'BRL', NULL, CURRENT_TIMESTAMP, 'PARSED');

INSERT INTO product_substance (product_id, substance_id, order_index, strength_text) VALUES
('40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 0, NULL),
('40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000005', 0, NULL);

INSERT INTO indication (id, slug, label_pt, label_en) VALUES
('50000000-0000-0000-0000-000000000001', 'synthetic-focus', 'Desfecho sintético de foco', 'Synthetic focus outcome'),
('50000000-0000-0000-0000-000000000002', 'synthetic-mood', 'Desfecho sintético de humor', 'Synthetic mood outcome');

INSERT INTO efficacy_assessment (id, substance_id, indication_id, population, outcome, efficacy_score, evidence_level, rationale_pt, rationale_en, rubric_version, review_status, publication_revision) VALUES
('51000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', 'População sintética adulta', 'Resultado sintético validado apenas para testes', 6, 'MODERATE', 'Pontuação fictícia para validar a interface; não é evidência médica.', 'Fictional score used to validate the interface; it is not medical evidence.', '1.0', 'APPROVED', 1),
('51000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000006', '50000000-0000-0000-0000-000000000002', 'População sintética adulta', 'Resultado sintético validado apenas para testes', 8, 'HIGH', 'Pontuação fictícia para validar a interface; não é evidência médica.', 'Fictional score used to validate the interface; it is not medical evidence.', '1.0', 'APPROVED', 2);

INSERT INTO risk_profile (id, substance_id, context_pt, context_en, overall_score, common_burden, severe_acute, chronic_organ, dependency_score, interaction_score, product_quality, regulatory_uncertainty, rationale_pt, rationale_en, rubric_version) VALUES
('60000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'Contexto sintético de uso rotulado', 'Synthetic labeled-use context', 3, 3, 2, 2, 1, 3, 1, 1, 'Perfil fictício para teste da escala.', 'Fictional profile for testing the scale.', '1.0'),
('60000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000005', 'Contexto sintético de alto risco', 'Synthetic high-risk context', 8, 6, 8, 8, 5, 7, 7, 8, 'Perfil fictício para teste da escala.', 'Fictional profile for testing the scale.', '1.0');

INSERT INTO source (id, title, source_type, url, jurisdiction, published_at, fetched_at, content_hash) VALUES
('70000000-0000-0000-0000-000000000001', 'Synthetic evidence fixture - not a medical source', 'SYNTHETIC_FIXTURE', 'https://example.invalid/substance-atlas/synthetic-evidence', NULL, NULL, CURRENT_TIMESTAMP, 'synthetic-fixture-v1');

INSERT INTO evidence_claim (id, substance_id, claim_pt, claim_en, extract_text, publication_revision) VALUES
('71000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'Afirmação sintética usada apenas para testar citações.', 'Synthetic claim used only to test citations.', NULL, 1),
('71000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000006', 'Afirmação sintética usada apenas para testar evidência de alta qualidade.', 'Synthetic claim used only to test high-quality evidence.', NULL, 2);

INSERT INTO evidence_claim_source (evidence_claim_id, source_id) VALUES
('71000000-0000-0000-0000-000000000001', '70000000-0000-0000-0000-000000000001'),
('71000000-0000-0000-0000-000000000002', '70000000-0000-0000-0000-000000000001');
