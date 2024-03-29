-- Trigger: UPDATE_AEAORCAM_ANT^

DROP TRIGGER UPDATE_AEAORCAM_ANT;

CREATE TRIGGER IF NOT EXISTS UPDATE_AEAORCAM_ANT BEFORE UPDATE ON AEAORCAM FOR EACH ROW ^
^BEGIN^ ^
	SELECT CASE WHEN ^
					((NEW.STATUS = 'P') ^
					AND ^
					((SELECT COUNT() FROM AEAITORC WHERE AEAITORC.ID_AEAORCAM = NEW.ID_AEAORCAM) <= 0))^
					^THEN^ RAISE (ABORT, 'Não existe produtos dentro do oçamento. Insira pelo meno um produto para transformar este orçamento em pedido.') ^
					^
				WHEN ^
					((NEW.STATUS = 'P') ^
					AND ^
					((SELECT AEAORCAM.ATAC_VAREJO FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM) = '0') ^
					AND ^
					((SELECT SMAEMPRE.FECHA_VENDA_CREDITO_NEGATIVO_ATACADO FROM SMAEMPRE WHERE SMAEMPRE.ID_SMAEMPRE = (SELECT AEAORCAM.ID_SMAEMPRE FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM)) = '0') ^
					AND ^
					((SELECT SMAEMPRE.TIPO_ACUMULO_CREDITO_ATACADO FROM SMAEMPRE WHERE ID_SMAEMPRE = (SELECT AEAORCAM.ID_SMAEMPRE FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM)) = 'P') ^
					AND ^
					((SELECT ROUND((((((SUM(FC_VL_TOTAL)) / (SUM(VL_TABELA))) * 100) - 100) * - 1), (IFNULL((SELECT QTD_CASAS_DECIMAIS FROM SMAEMPRE WHERE SMAEMPRE.ID_SMAEMPRE = (SELECT ID_SMAEMPRE FROM AEAORCAM WHERE ID_AEAORCAM = NEW.ID_AEAORCAM)), 3))) FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM) > 0)) ^
					^THEN^ (RAISE (ABORT, 'Não é permitido que o valor vendido seja menor que o valor de tabela nas vendas feitas no atacado. Reveja os descontos dos produtos. Neste caso o percentual de desconto tem que ser zerado.')) ^
				WHEN ^
					((NEW.STATUS = 'P') ^
					AND ^
					((SELECT AEAORCAM.ATAC_VAREJO FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM) = '0') ^
					AND ^
					((SELECT SMAEMPRE.FECHA_VENDA_CREDITO_NEGATIVO_ATACADO FROM SMAEMPRE WHERE SMAEMPRE.ID_SMAEMPRE = (SELECT AEAORCAM.ID_SMAEMPRE FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM)) = '0') ^
					AND ^
					((SELECT SMAEMPRE.TIPO_ACUMULO_CREDITO_ATACADO FROM SMAEMPRE WHERE ID_SMAEMPRE = (SELECT AEAORCAM.ID_SMAEMPRE FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM)) = 'V') ^
					AND ^
					((SELECT ROUND(((SUM(VL_TABELA)) - (SUM(FC_VL_TOTAL))), (IFNULL((SELECT QTD_CASAS_DECIMAIS FROM SMAEMPRE WHERE SMAEMPRE.ID_SMAEMPRE = (SELECT ID_SMAEMPRE FROM AEAORCAM WHERE ID_AEAORCAM = NEW.ID_AEAORCAM)), 3))) FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM) > 0)) ^
					^THEN^ (RAISE (ABORT, 'Não é permitido que o valor vendido seja menor que o valor de tabela nas vendas feita no atacado. Reveja os preços dos produtos. Neste caso o valor de desconto tem que ser zerado.')) ^
				WHEN ^
					((NEW.STATUS = 'P') ^
					AND ^
					((SELECT AEAORCAM.ATAC_VAREJO FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM) = '1') ^
					AND ^
					((SELECT SMAEMPRE.FECHA_VENDA_CREDITO_NEGATIVO_VAREJO FROM SMAEMPRE WHERE SMAEMPRE.ID_SMAEMPRE = (SELECT AEAORCAM.ID_SMAEMPRE FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM)) = '0') ^
					AND ^
					((SELECT SMAEMPRE.TIPO_ACUMULO_CREDITO_VAREJO FROM SMAEMPRE WHERE ID_SMAEMPRE = (SELECT AEAORCAM.ID_SMAEMPRE FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM)) = 'P') ^
					AND ^
					((SELECT ROUND(((SUM(VL_TABELA)) - (SUM(FC_VL_TOTAL))), (IFNULL((SELECT QTD_CASAS_DECIMAIS FROM SMAEMPRE WHERE SMAEMPRE.ID_SMAEMPRE = (SELECT ID_SMAEMPRE FROM AEAORCAM WHERE ID_AEAORCAM = NEW.ID_AEAORCAM)), 3))) FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM) > 0)) ^
					^THEN^ (RAISE (ABORT, 'Não é permitido que o valor vendido seja menor que o valor de tabela nas vendas feita no varejo. Reveja os descontos dos produtos. Neste caso o percentual de desconto tem que ser zerado.')) ^
				WHEN ^
					((NEW.STATUS = 'P') ^
					AND ^
					((SELECT AEAORCAM.ATAC_VAREJO FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM) = '1') ^
					AND ^
					((SELECT SMAEMPRE.FECHA_VENDA_CREDITO_NEGATIVO_VAREJO FROM SMAEMPRE WHERE SMAEMPRE.ID_SMAEMPRE = (SELECT AEAORCAM.ID_SMAEMPRE FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM)) = '0') ^
					AND ^
					((SELECT SMAEMPRE.TIPO_ACUMULO_CREDITO_VAREJO FROM SMAEMPRE WHERE ID_SMAEMPRE = (SELECT AEAORCAM.ID_SMAEMPRE FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM)) = 'V') ^
					AND ^
					((SELECT ROUND(((SUM(VL_TABELA)) - (SUM(FC_VL_TOTAL))), (IFNULL((SELECT QTD_CASAS_DECIMAIS FROM SMAEMPRE WHERE SMAEMPRE.ID_SMAEMPRE = (SELECT ID_SMAEMPRE FROM AEAORCAM WHERE ID_AEAORCAM = NEW.ID_AEAORCAM)), 3))) FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM) > 0)) ^
					^THEN^ (RAISE (ABORT, 'Não é permitido que o valor vendido seja menor que o valor de tabela nas vendas feita no varejo. Reveja os preços dos produtos. Neste caso o valor de desconto tem que ser zerado.')) ^
					END; ^
					^
	SELECT CASE WHEN ^
					((NEW.STATUS = 'P') ^
					AND ^
					((SELECT AVG(AEAPLPGT.DIAS_MEDIOS) FROM AEAITORC LEFT OUTER JOIN AEAPLPGT ON (AEAITORC.ID_AEAPLPGT = AEAPLPGT.ID_AEAPLPGT) WHERE AEAITORC.ID_AEAORCAM = NEW.ID_AEAORCAM) > 0) ^
					AND ^
					((SELECT ATAC_VAREJO FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM) = '0') ^
					AND ^
					((SELECT SMAEMPRE.VL_MIN_PRAZO_ATACADO FROM SMAEMPRE WHERE SMAEMPRE.ID_SMAEMPRE = (SELECT AEAORCAM.ID_SMAEMPRE FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM)) IS NOT NULL) ^
					AND ^
					((SELECT AEAORCAM.FC_VL_TOTAL FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM) <= (SELECT SMAEMPRE.VL_MIN_PRAZO_ATACADO FROM SMAEMPRE WHERE SMAEMPRE.ID_SMAEMPRE = (SELECT AEAORCAM.ID_SMAEMPRE FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM))) ^
					AND ^
					((SELECT FATURA_VL_MIN FROM CFAPARAM WHERE CFAPARAM.ID_CFACLIFO = NEW.ID_CFACLIFO) != '1')) ^
				^THEN^ RAISE (ABORT, 'O oçamento não atingio o valor mínimo exigido para as vendas a prazo no atacado.') ^
				END; ^
	SELECT CASE WHEN ^
					((NEW.STATUS = 'P') ^
					AND ^
					((SELECT AVG(AEAPLPGT.DIAS_MEDIOS) FROM AEAITORC LEFT OUTER JOIN AEAPLPGT ON (AEAITORC.ID_AEAPLPGT = AEAPLPGT.ID_AEAPLPGT) WHERE AEAITORC.ID_AEAORCAM = NEW.ID_AEAORCAM) <= 0) ^
					AND ^
					((SELECT ATAC_VAREJO FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM) = '0') ^
					AND ^
					((SELECT SMAEMPRE.VL_MIN_VISTA_ATACADO FROM SMAEMPRE WHERE SMAEMPRE.ID_SMAEMPRE = (SELECT AEAORCAM.ID_SMAEMPRE FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM)) IS NOT NULL) ^
					AND ^
					((SELECT AEAORCAM.FC_VL_TOTAL FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM) <= (SELECT SMAEMPRE.VL_MIN_VISTA_ATACADO FROM SMAEMPRE WHERE SMAEMPRE.ID_SMAEMPRE = (SELECT AEAORCAM.ID_SMAEMPRE FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM))) ^
					AND ^
					((SELECT FATURA_VL_MIN FROM CFAPARAM WHERE CFAPARAM.ID_CFACLIFO = NEW.ID_CFACLIFO) != '1')) ^
				^THEN^ RAISE (ABORT, 'O oçamento não atingio o valor mínimo exigido para as vendas a vista no atacado.')^
				END; ^
				^
	SELECT CASE WHEN ^
					((NEW.STATUS = 'P') ^
					AND ^
					((SELECT AVG(AEAPLPGT.DIAS_MEDIOS) FROM AEAITORC LEFT OUTER JOIN AEAPLPGT ON (AEAITORC.ID_AEAPLPGT = AEAPLPGT.ID_AEAPLPGT) WHERE AEAITORC.ID_AEAORCAM = NEW.ID_AEAORCAM) > 0) ^
					AND ^
					((SELECT ATAC_VAREJO FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM) = '1') ^
					AND ^
					((SELECT SMAEMPRE.VL_MIN_PRAZO_VAREJO FROM SMAEMPRE WHERE SMAEMPRE.ID_SMAEMPRE = (SELECT AEAORCAM.ID_SMAEMPRE FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM)) IS NOT NULL) ^
					AND ^
					((SELECT AEAORCAM.FC_VL_TOTAL FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM) <= (SELECT SMAEMPRE.VL_MIN_PRAZO_VAREJO FROM SMAEMPRE WHERE SMAEMPRE.ID_SMAEMPRE = (SELECT AEAORCAM.ID_SMAEMPRE FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM))) ^
					AND ^
					((SELECT FATURA_VL_MIN FROM CFAPARAM WHERE CFAPARAM.ID_CFACLIFO = NEW.ID_CFACLIFO) != '1')) ^
				^THEN^ RAISE (ABORT, 'O oçamento não atingio o valor mínimo exigido para as vendas a prazo no varejo.') ^
				END; ^
	SELECT CASE WHEN ^
					((NEW.STATUS = 'P') ^
					AND ^
					((SELECT AVG(AEAPLPGT.DIAS_MEDIOS) FROM AEAITORC LEFT OUTER JOIN AEAPLPGT ON (AEAITORC.ID_AEAPLPGT = AEAPLPGT.ID_AEAPLPGT) WHERE AEAITORC.ID_AEAORCAM = NEW.ID_AEAORCAM) <= 0) ^
					AND ^
					((SELECT ATAC_VAREJO FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM) = '1') ^
					AND ^
					((SELECT SMAEMPRE.VL_MIN_VISTA_VAREJO FROM SMAEMPRE WHERE SMAEMPRE.ID_SMAEMPRE = (SELECT AEAORCAM.ID_SMAEMPRE FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM)) IS NOT NULL) ^
					AND ^
					((SELECT AEAORCAM.FC_VL_TOTAL FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM) <= (SELECT SMAEMPRE.VL_MIN_VISTA_VAREJO FROM SMAEMPRE WHERE SMAEMPRE.ID_SMAEMPRE = (SELECT AEAORCAM.ID_SMAEMPRE FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM))) ^
					AND ^
					((SELECT FATURA_VL_MIN FROM CFAPARAM WHERE CFAPARAM.ID_CFACLIFO = NEW.ID_CFACLIFO) != '1')) ^
				^THEN^ RAISE (ABORT, 'O oçamento não atingio o valor mínimo exigido para as vendas a vista no varejo.') ^
				END; ^
	SELECT CASE WHEN ^
					((NEW.STATUS = 'P') ^
					AND ^
					((SELECT CFAPARAM.LIMITE FROM CFAPARAM WHERE CFAPARAM.ID_CFACLIFO = NEW.ID_CFACLIFO) IS NOT NULL) ^
					AND ^
					((SELECT CFAPARAM.LIMITE FROM CFAPARAM WHERE CFAPARAM.ID_CFACLIFO = NEW.ID_CFACLIFO) > 0) ^
                    AND ^
					(((SELECT SUM(RPAPARCE.FC_VL_RESTANTE) FROM RPAPARCE WHERE (RPAPARCE.ID_CFACLIFO = NEW.ID_CFACLIFO) AND (RPAPARCE.TIPO = 0) AND (RPAPARCE.DT_BAIXA IS NULL)) + (SELECT IFNULL((SELECT SUM(AEAITORC.FC_LIQUIDO) FROM AEAITORC, AEAPLPGT WHERE AEAITORC.ID_AEAPLPGT = AEAPLPGT.ID_AEAPLPGT AND AEAITORC.ID_AEAORCAM = NEW.ID_AEAORCAM AND AEAPLPGT.VISTA_PRAZO = '1'), 0)) ) > (SELECT CFAPARAM.LIMITE FROM CFAPARAM WHERE CFAPARAM.ID_CFACLIFO = NEW.ID_CFACLIFO))) ^
				^THEN^ RAISE (ABORT, 'O valor do orçamento ultrapassa o limite de compra da pessoa. Verifique o limite disponível nos dados cadastrais da pessoa.') ^
				END; ^
				^
	SELECT CASE WHEN ^
					((NEW.STATUS = 'P') ^
					AND ^
					((SELECT PARCELA_EM_ABERTO FROM CFAPARAM WHERE CFAPARAM.ID_CFACLIFO = NEW.ID_CFACLIFO) IS NOT NULL) ^
					AND ^
					((SELECT PARCELA_EM_ABERTO FROM CFAPARAM WHERE CFAPARAM.ID_CFACLIFO = NEW.ID_CFACLIFO) != '1') ^
					AND ^
					((SELECT COUNT() FROM RPAPARCE WHERE (RPAPARCE.ID_CFACLIFO = NEW.ID_CFACLIFO) AND (RPAPARCE.TIPO = 0) AND (RPAPARCE.DT_BAIXA IS NULL) ORDER BY RPAPARCE.DT_VENCIMENTO ASC) > 0)) ^
				^THEN^ RAISE (ABORT, 'Venda não permitida porque a pessoa esta com titulo(s) em aberto mesmo não estando(s) vencido(s).') ^
				END; ^
	SELECT CASE WHEN ^
					((NEW.STATUS = 'P') ^
					AND ^
					((SELECT SMAEMPRE.DIAS_ATRAZO FROM SMAEMPRE WHERE SMAEMPRE.ID_SMAEMPRE = (SELECT AEAORCAM.ID_SMAEMPRE FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM)) IS NOT NULL) ^
					AND ^
					((SELECT JULIANDAY(DATE('NOW', 'LOCALTIME')) - JULIANDAY(RPAPARCE.DT_VENCIMENTO) AS DIAS_VENCIDOS FROM RPAPARCE WHERE (RPAPARCE.ID_CFACLIFO = NEW.ID_CFACLIFO) AND (RPAPARCE.TIPO = 0) AND (RPAPARCE.DT_BAIXA IS NULL) AND (RPAPARCE.DT_VENCIMENTO < DATE('NOW', 'LOCALTIME')) ORDER BY RPAPARCE.DT_VENCIMENTO ASC LIMIT 1) >= (SELECT SMAEMPRE.DIAS_ATRAZO FROM SMAEMPRE WHERE SMAEMPRE.ID_SMAEMPRE = (SELECT AEAORCAM.ID_SMAEMPRE FROM AEAORCAM WHERE AEAORCAM.ID_AEAORCAM = NEW.ID_AEAORCAM)) AND ((SELECT VENDE_ATRAZADO FROM CFAPARAM WHERE CFAPARAM.ID_CFACLIFO = NEW.ID_CFACLIFO) != 1))) ^
				^THEN^ RAISE (ABORT, 'Venda não permitida por ultrapassar o limite de dias de atrazo. Existe titulo(s) atrazado(s).') ^
				END; ^
END~^
^